#include <string.h>
#include <jni.h>
#include <android/log.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <gst/gst.h>
#include <gst/interfaces/xoverlay.h>
#include <gst/video/video.h>
#include <pthread.h>
#include <glib.h>

GST_DEBUG_CATEGORY_STATIC( debug_category);
#define GST_CAT_DEFAULT debug_category

/*
 * These macros provide a way to store the native pointer to CustomData, which might be 32 or 64 bits, into
 * a jlong, which is always 64 bits, without warnings.
 */
#if GLIB_SIZEOF_VOID_P == 8
# define GET_CUSTOM_DATA(env, thiz, fieldID) (CustomData *)(*env)->GetLongField (env, thiz, fieldID)
# define SET_CUSTOM_DATA(env, thiz, fieldID, data) (*env)->SetLongField (env, thiz, fieldID, (jlong)data)
#else
# define GET_CUSTOM_DATA(env, thiz, fieldID) (CustomData *)(jint)(*env)->GetLongField (env, thiz, fieldID)
# define SET_CUSTOM_DATA(env, thiz, fieldID, data) (*env)->SetLongField (env, thiz, fieldID, (jlong)(jint)data)
#endif


#define APPNAME "by.gravity.doublexplayer"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, APPNAME, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, APPNAME, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, APPNAME, __VA_ARGS__)

/* Do not allow seeks to be performed closer than this distance. It is visually useless, and will probably
 * confuse some demuxers. */
#define SEEK_MIN_DELAY (1 * GST_MSECOND)

/* Structure to contain all our information, so we can pass it to callbacks */
typedef struct _CustomData {
	jobject app; /* Application instance, used to call its methods. A global reference is kept. */
	GstElement *pipeline; /* The running pipeline */
	GstElement *video_sink;
	GMainContext *context; /* GLib context used to run the main loop */
	GMainLoop *main_loop; /* GLib main loop */
	gboolean initialized; /* To avoid informing the UI multiple times about the initialization */
	ANativeWindow *native_window; /* The Android native window where video will be rendered */
	GstState state; /* Current pipeline state */
	GstState target_state; /* Desired pipeline state, to be set once buffering is complete */
	gint64 duration; /* Cached clip duration */
	gint64 desired_position; /* Position to seek to, once the pipeline is running */
	GstClockTime last_seek_time; /* For seeking overflow prevention (throttling) */
	gboolean is_live; /* Live streams do not use buffering */
	gdouble rate; /* Current playback rate (can be negative) */
	gboolean is_repeat; /*Repeat mode*/
} CustomData;

/* playbin2 flags */
typedef enum {
	GST_PLAY_FLAG_TEXT = (1 << 2) /* We want subtitle output */
} GstPlayFlags;

/* These global variables cache values which are not changing during execution */
static pthread_t gst_app_thread;
static pthread_key_t current_jni_env;
static JavaVM *java_vm;
static jfieldID custom_data_field_id;
static jmethodID set_message_method_id;
static jmethodID set_current_position_method_id;
static jmethodID on_gstreamer_initialized_method_id;
static jmethodID on_media_size_changed_method_id;
static jmethodID on_video_finished;
static jmethodID on_set_rate_finished;
/*
 * Private methods
 */

/* Register this thread with the VM */
static JNIEnv *attach_current_thread(void) {
	JNIEnv *env;
	JavaVMAttachArgs args;

	GST_DEBUG("Attaching thread %p", g_thread_self());
	args.version = JNI_VERSION_1_4;
	args.name = NULL;
	args.group = NULL;

	if ((*java_vm)->AttachCurrentThread(java_vm, &env, &args) < 0) {
		GST_ERROR("Failed to attach current thread");
		return NULL;
	}

	return env;
}

/* Unregister this thread from the VM */
static void detach_current_thread(void *env) {
	GST_DEBUG("Detaching thread %p", g_thread_self());
	(*java_vm)->DetachCurrentThread(java_vm);
}

/* Retrieve the JNI environment for this thread */
static JNIEnv *get_jni_env(void) {
	JNIEnv *env;

	if ((env = pthread_getspecific(current_jni_env)) == NULL) {
		env = attach_current_thread();
		pthread_setspecific(current_jni_env, env);
	}

	return env;
}

/* Change the content of the UI's TextView */
static void set_ui_message(const gchar *message, CustomData *data) {
	JNIEnv *env = get_jni_env();
	GST_DEBUG("Setting message to: %s", message);
	jstring jmessage = (*env)->NewStringUTF(env, message);
	(*env)->CallVoidMethod(env, data->app, set_message_method_id, jmessage);
	if ((*env)->ExceptionCheck(env)) {
		GST_ERROR("Failed to call Java method");
		(*env)->ExceptionClear(env);
	}
	(*env)->DeleteLocalRef(env, jmessage);
}

/* Tell the application what is the current position and clip duration */
static void set_current_ui_position(gint position, gint duration,
		CustomData *data) {
	JNIEnv *env = get_jni_env();
	(*env)->CallVoidMethod(env, data->app, set_current_position_method_id,
			position, duration);
	if ((*env)->ExceptionCheck(env)) {
		GST_ERROR("Failed to call Java method");
		(*env)->ExceptionClear(env);
	}
}

static void video_finished(CustomData *data) {
	JNIEnv *env = get_jni_env();
	(*env)->CallVoidMethod(env, data->app, on_video_finished);
}

static void set_rate_finished(CustomData *data){
	JNIEnv *env = get_jni_env();
		(*env)->CallVoidMethod(env, data->app, on_set_rate_finished);
}

/* If we have pipeline and it is running, query the current position and clip duration and inform
 * the application */
static gboolean refresh_ui(CustomData *data) {
	GstFormat fmt = GST_FORMAT_TIME;
	gint64 current = -1;
	gint64 position;

	/* We do not want to update anything unless we have a working pipeline in the PAUSED or PLAYING state */
	if (!data || !data->pipeline || data->state < GST_STATE_PAUSED)
		return TRUE;

	/* If we didn't know it yet, query the stream duration */
	if (!GST_CLOCK_TIME_IS_VALID(data->duration)) {
		if (!gst_element_query_duration(data->pipeline, &fmt,
				&data->duration)) {
			GST_WARNING(
					"Could not query current duration (normal for still pictures)");
			data->duration = 0;

		}
	}

	if (!gst_element_query_position(data->pipeline, &fmt, &position)) {
		GST_WARNING(
				"Could not query current position (normal for still pictures)");
		position = 0;

	}


	/* Java expects these values in milliseconds, and GStreamer provides nanoseconds */
	set_current_ui_position(position / GST_MSECOND,
			data->duration / GST_MSECOND, data);
	return TRUE;
}

/* Forward declaration for the delayed seek callback */
static gboolean delayed_seek_cb(CustomData *data);

/* Perform seek, if we are not too close to the previous seek. Otherwise, schedule the seek for
 * some time in the future. */
static void execute_seek(gint64 desired_position, CustomData *data) {
	gint64 diff;

	if (desired_position == GST_CLOCK_TIME_NONE)
		return;

	GST_DEBUG(
				"Seeking to %" GST_TIME_FORMAT, GST_TIME_ARGS (desired_position));
//	gst_element_seek_simple(data->pipeline, GST_FORMAT_TIME,
//				GST_SEEK_FLAG_FLUSH | GST_SEEK_FLAG_ACCURATE, desired_position);

	GstFormat format = GST_FORMAT_TIME;
	GstEvent *seek_event;
	    seek_event = gst_event_new_seek (data->rate, GST_FORMAT_TIME, GST_SEEK_FLAG_FLUSH | GST_SEEK_FLAG_ACCURATE,
	        GST_SEEK_TYPE_SET, desired_position, GST_SEEK_TYPE_NONE, 0);

	  if (data->video_sink == NULL) {
	    /* If we have not done so, obtain the sink through which we will send the seek events */
	    g_object_get (data->pipeline, "video-sink", &data->video_sink, NULL);
	  }

	  /* Send the event */
	  gst_element_send_event (data->video_sink, seek_event);
}

/* Delayed seek callback. This gets called by the timer setup in the above function. */
static gboolean delayed_seek_cb(CustomData *data) {
	GST_DEBUG(
			"Doing delayed seek to %" GST_TIME_FORMAT, GST_TIME_ARGS (data->desired_position));
	execute_seek(data->desired_position, data);
	return FALSE;
}

/* Retrieve errors from the bus and show them on the UI */
static void error_cb(GstBus *bus, GstMessage *msg, CustomData *data) {
	GError *err;
	gchar *debug_info;
	gchar *message_string;

	gst_message_parse_error(msg, &err, &debug_info);
	message_string = g_strdup_printf("Error received from element %s: %s",
			GST_OBJECT_NAME(msg->src), err->message);
	g_clear_error(&err);
	g_free(debug_info);
	set_ui_message(message_string, data);
	g_free(message_string);
	data->target_state = GST_STATE_NULL;
	gst_element_set_state(data->pipeline, GST_STATE_NULL);
}

/* Called when the End Of the Stream is reached. Just move to the beginning of the media and pause. */
static void eos_cb(GstBus *bus, GstMessage *msg, CustomData *data) {
	jdouble rate = data->rate;
	execute_seek(0, data);
	data->target_state = GST_STATE_PAUSED;
	data->is_live = (gst_element_set_state(data->pipeline, GST_STATE_PAUSED)
			== GST_STATE_CHANGE_NO_PREROLL);
	video_finished(data);
	/*
	if(data->is_repeat){
		g_print("set state to ready");
		data->target_state = GST_STATE_READY;
		data->is_live = (gst_element_set_state(data->pipeline, GST_STATE_READY)
			== GST_STATE_CHANGE_NO_PREROLL);
		data->target_state = GST_STATE_PLAYING;
		data->is_live = (gst_element_set_state(data->pipeline, GST_STATE_PLAYING)
			== GST_STATE_CHANGE_NO_PREROLL);
		data->rate = rate;
		send_seek_event(data);
		g_print("start from start");
	}*/
	g_print("end of video");
}

/* Called when the duration of the media changes. Just mark it as unknown, so we re-query it in the next UI refresh. */
static void duration_cb(GstBus *bus, GstMessage *msg, CustomData *data) {
	data->duration = GST_CLOCK_TIME_NONE;
}

/* Called when buffering messages are received. We inform the UI about the current buffering level and
 * keep the pipeline paused until 100% buffering is reached. At that point, set the desired state. */
static void buffering_cb(GstBus *bus, GstMessage *msg, CustomData *data) {
	gint percent;

	if (data->is_live)
		return;

	gst_message_parse_buffering(msg, &percent);
	if (percent < 100 && data->target_state >= GST_STATE_PAUSED) {
		gchar * message_string = g_strdup_printf("Buffering %d%%", percent);
		gst_element_set_state(data->pipeline, GST_STATE_PAUSED);
		set_ui_message(message_string, data);
		g_free(message_string);
	} else if (data->target_state >= GST_STATE_PLAYING) {
		gst_element_set_state(data->pipeline, GST_STATE_PLAYING);
	} else if (data->target_state >= GST_STATE_PAUSED) {
		set_ui_message("Buffering complete", data);
	}
}

/* Called when the clock is lost */
static void clock_lost_cb(GstBus *bus, GstMessage *msg, CustomData *data) {
	if (data->target_state >= GST_STATE_PLAYING) {
		gst_element_set_state(data->pipeline, GST_STATE_PAUSED);
		gst_element_set_state(data->pipeline, GST_STATE_PLAYING);
	}
}

/* Retrieve the video sink's Caps and tell the application about the media size */
static void check_media_size(CustomData *data) {
	/*
	JNIEnv *env = get_jni_env();
	GstElement *video_sink;
	GstPad *video_sink_pad;
	GstCaps *caps;
	GstVideoFormat fmt;
	int width;
	int height;

	/* Retrieve the Caps at the entrance of the video sink
	g_object_get(data->pipeline, "video-sink", &video_sink, NULL);
	video_sink_pad = gst_element_get_static_pad(video_sink, "sink");
	caps = gst_pad_get_negotiated_caps(video_sink_pad);

	if (gst_video_format_parse_caps(caps, &fmt, &width, &height)) {
		int par_n, par_d;
		if (gst_video_parse_caps_pixel_aspect_ratio(caps, &par_n, &par_d)) {
			width = width * par_n / par_d;
		}
		GST_DEBUG("Media size is %dx%d, notifying application", width, height);

		(*env)->CallVoidMethod(env, data->app, on_media_size_changed_method_id,
				(jint) width, (jint) height);
		if ((*env)->ExceptionCheck(env)) {
			GST_ERROR("Failed to call Java method");
			(*env)->ExceptionClear(env);
		}
	}

	gst_caps_unref(caps);
	gst_object_unref(video_sink_pad);
	gst_object_unref(video_sink);*/
}

/* Notify UI about pipeline state changes */
static void state_changed_cb(GstBus *bus, GstMessage *msg, CustomData *data) {
	GstState old_state, new_state, pending_state;
	gst_message_parse_state_changed(msg, &old_state, &new_state,
			&pending_state);
	/* Only pay attention to messages coming from the pipeline, not its children */
	if (GST_MESSAGE_SRC(msg) == GST_OBJECT(data->pipeline)) {
		data->state = new_state;
		gchar *message = g_strdup_printf("State changed to %s",
				gst_element_state_get_name(new_state));
		set_ui_message(message, data);
		g_free(message);

		/* The Ready to Paused state change is particularly interesting: */
		if (old_state == GST_STATE_READY && new_state == GST_STATE_PAUSED) {
			/* By now the sink already knows the media size */
			check_media_size(data);

			/* If there was a scheduled seek, perform it now that we have moved to the Paused state */
			if (GST_CLOCK_TIME_IS_VALID(data->desired_position))
				execute_seek(data->desired_position, data);
		}
	}
}

/* Check if all conditions are met to report GStreamer as initialized.
 * These conditions will change depending on the application */
static void check_initialization_complete(CustomData *data) {
	JNIEnv *env = get_jni_env();
	if (!data->initialized && data->native_window && data->main_loop) {
		GST_DEBUG(
				"Initialization complete, notifying application. native_window:%p main_loop:%p", data->native_window, data->main_loop);

		/* The main loop is running and we received a native window, inform the sink about it */
		gst_x_overlay_set_window_handle(GST_X_OVERLAY(data->pipeline),
				(guintptr) data->native_window);

		(*env)->CallVoidMethod(env, data->app,
				on_gstreamer_initialized_method_id);
		if ((*env)->ExceptionCheck(env)) {
			GST_ERROR("Failed to call Java method");
			(*env)->ExceptionClear(env);
		}
		data->initialized = TRUE;
	}
}

/* Main method for the native code. This is executed on its own thread. */
static void *app_function(void *userdata) {
	JavaVMAttachArgs args;
	GstBus *bus;
	CustomData *data = (CustomData *) userdata;
	GSource *timeout_source;
	GSource *bus_source;
	GError *error = NULL;
	guint flags;

	GST_DEBUG("Creating pipeline in CustomData at %p", data);

	/* Create our own GLib Main Context and make it the default one */
	data->context = g_main_context_new();
	g_main_context_push_thread_default(data->context);

	/* Build pipeline */
	data->pipeline = gst_parse_launch("playbin2", &error);
	if (error) {
		gchar *message = g_strdup_printf("Unable to build pipeline: %s",
				error->message);
		g_clear_error(&error);
		set_ui_message(message, data);
		g_free(message);
		return NULL;
	}

	/* Disable subtitles */
	g_object_get(data->pipeline, "flags", &flags, NULL);
	flags &= ~GST_PLAY_FLAG_TEXT;
	g_object_set(data->pipeline, "flags", flags, NULL);

	/* Set the pipeline to READY, so it can already accept a window handle, if we have one */
	data->target_state = GST_STATE_READY;
	gst_element_set_state(data->pipeline, GST_STATE_READY);

	/* Instruct the bus to emit signals for each received message, and connect to the interesting signals */
	bus = gst_element_get_bus(data->pipeline);
	bus_source = gst_bus_create_watch(bus);
	g_source_set_callback(bus_source, (GSourceFunc) gst_bus_async_signal_func,
			NULL, NULL);
	g_source_attach(bus_source, data->context);
	g_source_unref(bus_source);
	g_signal_connect(G_OBJECT(bus), "message::error", (GCallback) error_cb,
			data);
	g_signal_connect(G_OBJECT(bus), "message::eos", (GCallback) eos_cb, data);
	g_signal_connect(G_OBJECT(bus), "message::state-changed",
			(GCallback) state_changed_cb, data);
	g_signal_connect(G_OBJECT(bus), "message::duration",
			(GCallback) duration_cb, data);
	g_signal_connect(G_OBJECT(bus), "message::buffering",
			(GCallback) buffering_cb, data);
	g_signal_connect(G_OBJECT(bus), "message::clock-lost",
			(GCallback) clock_lost_cb, data);
	gst_object_unref(bus);

	/* Register a function that GLib will call 4 times per second */
	timeout_source = g_timeout_source_new(250);
	g_source_set_callback(timeout_source, (GSourceFunc) refresh_ui, data, NULL);
	g_source_attach(timeout_source, data->context);
	g_source_unref(timeout_source);

	/* Create a GLib Main Loop and set it to run */
	GST_DEBUG("Entering main loop... (CustomData:%p)", data);
	data->main_loop = g_main_loop_new(data->context, FALSE);
	check_initialization_complete(data);
	g_main_loop_run(data->main_loop);
	GST_DEBUG("Exited main loop");
	g_main_loop_unref(data->main_loop);
	data->main_loop = NULL;

	/* Free resources */
	g_main_context_pop_thread_default(data->context);
	g_main_context_unref(data->context);
	data->target_state = GST_STATE_NULL;
	gst_element_set_state(data->pipeline, GST_STATE_NULL);
	gst_object_unref(data->pipeline);

	return NULL;
}

/*
 * Java Bindings
 */

/* Instruct the native code to create its internal data structure, pipeline and thread */
static void gst_native_init(JNIEnv* env, jobject thiz) {
	CustomData *data = g_new0(CustomData, 1);
	data->desired_position = GST_CLOCK_TIME_NONE;
	data->last_seek_time = GST_CLOCK_TIME_NONE;
	SET_CUSTOM_DATA(env, thiz, custom_data_field_id, data);
	GST_DEBUG_CATEGORY_INIT(debug_category, "tutorial-5", 0,
			"Android tutorial 5");
	gst_debug_set_threshold_for_name("tutorial-5", GST_LEVEL_DEBUG);
	GST_DEBUG("Created CustomData at %p", data);
	data->app = (*env)->NewGlobalRef(env, thiz);
	GST_DEBUG("Created GlobalRef for app object at %p", data->app);
	pthread_create(&gst_app_thread, NULL, &app_function, data);
}

/* Quit the main loop, remove the native thread and free resources */
static void gst_native_finalize(JNIEnv* env, jobject thiz) {
	g_print("calling native finalize");
	CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
	if (!data)
		return;

	GST_DEBUG("Quitting main loop...");
	g_main_loop_quit(data->main_loop);
	GST_DEBUG("Waiting for thread to finish...");
	pthread_join(gst_app_thread, NULL);
	GST_DEBUG("Deleting GlobalRef for app object at %p", data->app);
	(*env)->DeleteGlobalRef(env, data->app);
	GST_DEBUG("Freeing CustomData at %p", data);
	g_free(data);
	SET_CUSTOM_DATA(env, thiz, custom_data_field_id, NULL);
	GST_DEBUG("Done finalizing");

}

/* Set playbin2's URI */
void gst_native_set_uri(JNIEnv* env, jobject thiz, jstring uri) {
	CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
	if (!data || !data->pipeline)
		return;
	const jbyte *char_uri = (*env)->GetStringUTFChars(env, uri, NULL);
	GST_DEBUG("Setting URI to %s", char_uri);
	if (data->target_state >= GST_STATE_READY)
		gst_element_set_state(data->pipeline, GST_STATE_READY);
	g_object_set(data->pipeline, "uri", char_uri, NULL);
	(*env)->ReleaseStringUTFChars(env, uri, char_uri);
	data->duration = GST_CLOCK_TIME_NONE;
	data->is_live = (gst_element_set_state(data->pipeline, data->target_state)
			== GST_STATE_CHANGE_NO_PREROLL);
}

/* Set pipeline to PLAYING state */
static void gst_native_play(JNIEnv* env, jobject thiz) {
	CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
	if (!data)
		return;
	GST_DEBUG("Setting state to PLAYING");
	data->target_state = GST_STATE_PLAYING;
	data->is_live = (gst_element_set_state(data->pipeline, GST_STATE_PLAYING)
			== GST_STATE_CHANGE_NO_PREROLL);
}

/* Set pipeline to PAUSED state */
static void gst_native_pause(JNIEnv* env, jobject thiz) {
	CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
	if (!data)
		return;
	GST_DEBUG("Setting state to PAUSED");
	data->target_state = GST_STATE_PAUSED;
	data->is_live = (gst_element_set_state(data->pipeline, GST_STATE_PAUSED)
			== GST_STATE_CHANGE_NO_PREROLL);
}

/* Instruct the pipeline to seek to a different position */
void gst_native_set_position(JNIEnv* env, jobject thiz, int milliseconds) {
	CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
	if (!data)
		return;
	gint64 desired_position = (gint64) (milliseconds * GST_MSECOND);
	//if (data->state >= GST_STATE_PAUSED) {
		execute_seek(desired_position, data);
	/*} else {
		GST_DEBUG(
				"Scheduling seek to %" GST_TIME_FORMAT " for later", GST_TIME_ARGS (desired_position));
		data->desired_position = desired_position;
	}*/
}

/* Static class initializer: retrieve method and field IDs */
static jboolean gst_native_class_init(JNIEnv* env, jclass klass) {
	custom_data_field_id = (*env)->GetFieldID(env, klass, "native_custom_data",
			"J");
	set_message_method_id = (*env)->GetMethodID(env, klass, "setMessage",
			"(Ljava/lang/String;)V");
	set_current_position_method_id = (*env)->GetMethodID(env, klass,
			"setCurrentPosition", "(II)V");
	on_gstreamer_initialized_method_id = (*env)->GetMethodID(env, klass,
			"onGStreamerInitialized", "()V");
	on_media_size_changed_method_id = (*env)->GetMethodID(env, klass,
			"onMediaSizeChanged", "(II)V");
	on_video_finished = (*env)->GetMethodID(env, klass,
				"onVideoFinished", "()V");
	on_set_rate_finished = (*env)->GetMethodID(env, klass,
			"onSetRateFinished", "()V");

	if (!custom_data_field_id || !set_message_method_id
			|| !on_gstreamer_initialized_method_id
			|| !on_media_size_changed_method_id
			|| !set_current_position_method_id
			|| !on_video_finished
			|| !on_set_rate_finished) {
		/* We emit this message through the Android log instead of the GStreamer log because the later
		 * has not been initialized yet.
		 */
		__android_log_print(ANDROID_LOG_ERROR, "tutorial-4",
				"The calling class does not implement all necessary interface methods");
		return JNI_FALSE;
	}
	return JNI_TRUE;
}

static void gst_native_surface_init(JNIEnv *env, jobject thiz, jobject surface) {
	CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
	if (!data)
		return;
	ANativeWindow *new_native_window = ANativeWindow_fromSurface(env, surface);
	GST_DEBUG(
			"Received surface %p (native window %p)", surface, new_native_window);

	if (data->native_window) {
		ANativeWindow_release(data->native_window);
		if (data->native_window == new_native_window) {
			GST_DEBUG(
					"New native window is the same as the previous one", data->native_window);
			if (data->pipeline) {
				gst_x_overlay_expose(GST_X_OVERLAY(data->pipeline));
				gst_x_overlay_expose(GST_X_OVERLAY(data->pipeline));
			}
			return;
		} else {
			GST_DEBUG(
					"Released previous native window %p", data->native_window);
			data->initialized = FALSE;
		}
	}
	data->native_window = new_native_window;

	check_initialization_complete(data);
}

static void gst_native_surface_finalize(JNIEnv *env, jobject thiz) {
	CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
	if (!data)
		return;
	GST_DEBUG("Releasing Native Window %p", data->native_window);

	if (data->pipeline) {
		gst_x_overlay_set_window_handle(GST_X_OVERLAY(data->pipeline),
				(guintptr) NULL);
		gst_element_set_state(data->pipeline, GST_STATE_READY);
	}

	ANativeWindow_release(data->native_window);
	data->native_window = NULL;
	data->initialized = FALSE;
}

void gst_native_set_rate(JNIEnv *env, jobject thiz, jdouble rate) {
	CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
	if (!data) {
		return;
	}

	GstState currentState = data->target_state ;
	if(currentState == GST_STATE_PLAYING){
		g_print("video played set state pause");
		data->target_state = GST_STATE_PAUSED;
		data->is_live = (gst_element_set_state(data->pipeline, GST_STATE_PAUSED)
					== GST_STATE_CHANGE_NO_PREROLL);
	}

	data->rate = rate;
	send_seek_event(data);
	/*
	sleep(1);
	if(currentState == GST_STATE_PLAYING){
		g_print("continue playing");
		data->target_state = GST_STATE_PLAYING;
		data->is_live = (gst_element_set_state(data->pipeline, GST_STATE_PLAYING)
							== GST_STATE_CHANGE_NO_PREROLL);

	}
	*/
	set_rate_finished(data);

}

/* Send seek event to change rate */
void send_seek_event (CustomData *data) {
  gint64 position;
  GstFormat format = GST_FORMAT_TIME;
  GstEvent *seek_event;

  /* Obtain the current position, needed for the seek event */
  if (!gst_element_query_position (data->pipeline, &format, &position)) {
    g_printerr ("Unable to retrieve current position.\n");
    return;
  }

  /* Create the seek event */
  if (data->rate > 0) {
    seek_event = gst_event_new_seek (data->rate, GST_FORMAT_TIME, GST_SEEK_FLAG_FLUSH | GST_SEEK_FLAG_ACCURATE,
        GST_SEEK_TYPE_SET, position, GST_SEEK_TYPE_NONE, 0);
  } else {
    seek_event = gst_event_new_seek (data->rate, GST_FORMAT_TIME, GST_SEEK_FLAG_FLUSH | GST_SEEK_FLAG_ACCURATE,
        GST_SEEK_TYPE_SET, 0, GST_SEEK_TYPE_SET, position);
  }

  if (data->video_sink == NULL) {
    /* If we have not done so, obtain the sink through which we will send the seek events */
    g_object_get (data->pipeline, "video-sink", &data->video_sink, NULL);
  }

  /* Send the event */
  gst_element_send_event (data->video_sink, seek_event);

  g_print ("Current rate: %g\n", data->rate);
}

void gst_native_next_frame(JNIEnv *env, jobject thiz) {
	CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
	if (!data) {
		return;
	}
	if (data->video_sink == NULL) {
		/* If we have not done so, obtain the sink through which we will send the step events */
		g_object_get(data->pipeline, "video-sink", &data->video_sink, NULL);
	}

	gst_element_send_event(data->video_sink,
			gst_event_new_step(GST_FORMAT_BUFFERS, 1, data->rate, TRUE, FALSE));
	g_print("Stepping one frame\n");
}

void gst_seek_to_position(JNIEnv *env, jobject thiz,gint milliseconds) {
	CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
	if (!data) {
		return;
	}


	gint64 desired_position = (gint64) (milliseconds * GST_MSECOND);

	GstEvent *seek_event = gst_event_new_seek (data->rate, GST_FORMAT_TIME, GST_SEEK_FLAG_FLUSH | GST_SEEK_FLAG_ACCURATE,
	        GST_SEEK_TYPE_SET, desired_position, GST_SEEK_TYPE_NONE, 0);

	  if (data->video_sink == NULL) {
	    /* If we have not done so, obtain the sink through which we will send the seek events */
	    g_object_get (data->pipeline, "video-sink", &data->video_sink, NULL);
	  }

	  /* Send the event */
	  gst_element_send_event (data->video_sink, seek_event);
}

void gst_native_set_repeat_mode(JNIEnv *env, jobject thiz,jboolean is_repeat){
	CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
	if (!data) {
		return;
	}
	data->is_repeat = is_repeat;
	g_print("set repeat mode");
}

void gst_native_set_fragment(JNIEnv *env, jobject thiz,gint startPosition,gint finishPosition){

	CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
	if (!data) {
			return;
	}
	g_print("set fragment start position %d finish position %d",startPosition,finishPosition);
	gint64 start_position = (gint64) (startPosition * GST_MSECOND);
	gint64 finish_position = (gint64) (finishPosition * GST_MSECOND);
	GstEvent *seek_event = gst_event_new_seek (data->rate, GST_FORMAT_TIME, GST_SEEK_FLAG_FLUSH | GST_SEEK_FLAG_ACCURATE,
			GST_SEEK_TYPE_SET , start_position, GST_SEEK_TYPE_SET, finish_position);

	if (data->video_sink == NULL) {
		/* If we have not done so, obtain the sink through which we will send the seek events */
		g_object_get (data->pipeline, "video-sink", &data->video_sink, NULL);
	}

	/* Send the event */
	gst_element_send_event (data->video_sink, seek_event);

}



/* List of implemented native methods */
static JNINativeMethod native_methods[] = { { "nativeInit", "()V",
		(void *) gst_native_init }, { "nativeFinalize", "()V",
		(void *) gst_native_finalize }, { "nativeSetUri",
		"(Ljava/lang/String;)V", (void *) gst_native_set_uri }, { "nativePlay",
		"()V", (void *) gst_native_play }, { "nativePause", "()V",
		(void *) gst_native_pause }, { "nativeSetPosition", "(I)V",
		(void*) gst_native_set_position }, { "nativeSurfaceInit",
		"(Ljava/lang/Object;)V", (void *) gst_native_surface_init }, {
		"nativeSurfaceFinalize", "()V", (void *) gst_native_surface_finalize },
		{ "nativeClassInit", "()Z", (void *) gst_native_class_init }, {
				"nativeSetRate", "(D)V", gst_native_set_rate }, {
				"nativeNextFrame", "()V", (void *) gst_native_next_frame }, {
				"nativeSetRepeatMode", "(Z)V", (void *) gst_native_set_repeat_mode },{
						"nativeSeekToPosition", "(I)V", (void *) gst_seek_to_position },{
								"nativeSetFragment", "(II)V", (void *) gst_native_set_fragment }};

/* Library initializer */
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
	JNIEnv *env = NULL;

	java_vm = vm;

	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
		__android_log_print(ANDROID_LOG_ERROR, "tutorial-5",
				"Could not retrieve JNIEnv");
		return 0;
	}
	jclass klass = (*env)->FindClass(env,
			"by/gravity/doubleplayer/core/fragment/NativeVideoFragment");
	(*env)->RegisterNatives(env, klass, native_methods,
			G_N_ELEMENTS(native_methods));

	pthread_key_create(&current_jni_env, detach_current_thread);

	return JNI_VERSION_1_4;
}
