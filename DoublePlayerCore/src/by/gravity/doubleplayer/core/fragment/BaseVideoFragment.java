package by.gravity.doubleplayer.core.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import by.gravity.doubleplayer.core.IPlayer;
import by.gravity.doubleplayer.core.utils.StringUtil;
import by.gravity.doubleplayer.core.view.GStreamerSurfaceView;

import com.gstreamer.GStreamer;

abstract public class BaseVideoFragment extends NativeVideoFragment implements SurfaceHolder.Callback, OnSeekBarChangeListener, IPlayer {

	private static final String TAG = BaseVideoFragment.class.getSimpleName();

	private boolean is_local_media; // Whether this clip is stored locally or is
									// being streamed
	private int desired_position; // Position where the users wants to seek to
	private String mediaUri; // URI of the clip being played

	private boolean is_playing_desired; // Whether the user asked to go to
	// PLAYING

	private PowerManager.WakeLock wake_lock;

	abstract public int getSurfaceID();

	abstract public int getViewID();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		gstreamerInit();

	}

	private void gstreamerInit() {
		try {
			GStreamer.init(getActivity());
		} catch (Exception e) {
			Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
			getActivity().finish();
			return;
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
		wake_lock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);
		wake_lock.setReferenceCounted(false);

		SurfaceView sv = (SurfaceView) getView().findViewById(getSurfaceID());
		SurfaceHolder sh = sv.getHolder();
		sh.addCallback(this);

//		SeekBar sb = (SeekBar) getView().findViewById(R.id.seek_bar);
//		sb.setOnSeekBarChangeListener(this);

		// Retrieve our previous state, or initialize it to default values
		if (savedInstanceState != null) {
			is_playing_desired = savedInstanceState.getBoolean("playing");
			setPosition(savedInstanceState.getInt("position"));
			setDuration(savedInstanceState.getInt("duration"));
			mediaUri = savedInstanceState.getString("mediaUri");
		} else {
			is_playing_desired = false;
			setPosition(0);
			setDuration(0);

		}
		is_local_media = false;

		nativeInit();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(getViewID(), container);
	}

	@Override
	public void onDestroyView() {
		nativeFinalize();
		if (wake_lock.isHeld()) {
			wake_lock.release();
		}
		super.onDestroy();

	}

	// Called from native code
	@Override
	protected void onGStreamerInitialized() {
		Log.i("GStreamer", "GStreamer initialized:");

		// Restore previous playing state
		setMediaUri(mediaUri);
		setPosition(getPosition());
		if (is_playing_desired) {
			play();
		} else {
			pause();
		}

	}

	// The text widget acts as an slave for the seek bar, so it reflects what
	// the seek bar shows, whether
	// it is an actual pipeline position or the position the user is currently
	// dragging to.
	private void updateTimeWidget() {
		// TextView tv = (TextView) getView().findViewById(R.id.textview_time);
		// SeekBar sb = (SeekBar) getView().findViewById(R.id.seek_bar);
		// int pos = sb.getProgress();
		//
		// SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
		// df.setTimeZone(TimeZone.getTimeZone("UTC"));
		// String message = df.format(new Date(pos)) + " / " + df.format(new
		// Date(getDuration()));
		// tv.setText(message);
	}

	// Called from native code
	@Override
	protected void setCurrentPosition(final int position, final int duration) {
//		final SeekBar sb = (SeekBar) getView().findViewById(R.id.seek_bar);

		// Ignore position messages from the pipeline if the seek bar is being
		// dragged
		// if (sb.isPressed())
		// return;
		//
		// getActivity().runOnUiThread(new Runnable() {
		// public void run() {
		// sb.setMax(duration);
		// sb.setProgress(position);
		// updateTimeWidget();
		// sb.setEnabled(duration != 0);
		// }
		// });
		setPosition(position);
		setDuration(duration);
	}

	static {
		System.loadLibrary("gstreamer_android");
		System.loadLibrary("doublePlayer");
		nativeClassInit();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d("GStreamer", "Surface changed to format " + format + " width " + width + " height " + height);
		nativeSurfaceInit(holder.getSurface());
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.d("GStreamer", "Surface created: " + holder.getSurface());
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d("GStreamer", "Surface destroyed");
		nativeSurfaceFinalize();
	}

	// Called from native code
	@Override
	protected void onMediaSizeChanged(int width, int height) {
		Log.i("GStreamer", "Media size changed to " + width + "x" + height);
		final GStreamerSurfaceView gsv = (GStreamerSurfaceView) getView().findViewById(getSurfaceID());
		gsv.media_width = width;
		gsv.media_height = height;
		getActivity().runOnUiThread(new Runnable() {
			public void run() {
				gsv.requestLayout();
			}
		});
	}

	// The Seek Bar thumb has moved, either because the user dragged it or we
	// have called setProgress()
	public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
		if (fromUser == false)
			return;
		desired_position = progress;
		// If this is a local file, allow scrub seeking, this is, seek as soon
		// as the slider is moved.
		if (is_local_media) {
			nativeSetPosition(desired_position);
		}
		updateTimeWidget();
	}

	// The user started dragging the Seek Bar thumb
	public void onStartTrackingTouch(SeekBar sb) {
		nativePause();
	}

	// The user released the Seek Bar thumb
	public void onStopTrackingTouch(SeekBar sb) {
		// If this is a remote file, scrub seeking is probably not going to work
		// smoothly enough.
		// Therefore, perform only the seek when the slider is released.
		if (!is_local_media)
			nativeSetPosition(desired_position);
		if (is_playing_desired)
			nativePlay();
	}

	@Override
	public void setRate(double rate) {
		nativeSetRate(rate);

	}

	@Override
	public void play() {
		is_playing_desired = true;
		wake_lock.acquire();
		nativePlay();
	}

	@Override
	public void pause() {
		is_playing_desired = false;
		wake_lock.release();
		nativePause();

	}

	@Override
	public void setMediaUri(String uri) {
		if (!StringUtil.isEmpty(uri)) {
			mediaUri = uri;
			nativeSetUri(mediaUri);
			is_local_media = uri.startsWith("file://");
		}

	}

}
