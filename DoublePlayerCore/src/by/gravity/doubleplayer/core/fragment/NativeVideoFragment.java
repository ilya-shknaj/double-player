package by.gravity.doubleplayer.core.fragment;

import android.support.v4.app.Fragment;
import android.util.Log;

public class NativeVideoFragment extends Fragment {

	private static final String TAG = NativeVideoFragment.class.getSimpleName();

	protected native void nativeInit(); // Initialize native code, build
										// pipeline,
	// etc

	protected native void nativeFinalize(); // Destroy pipeline and shutdown
	// native code

	protected native void nativeSetUri(String uri); // Set the URI of the media
													// to
	// play

	protected native void nativePlay(); // Set pipeline to PLAYING

	protected native void nativeSetPosition(int milliseconds); // Seek to the
																// indicate
																// position, in
																// milliseconds

	protected native void nativePause(); // Set pipeline to PAUSED
	
	protected native void nativeSetRate(double rate); // set playback rate

	protected static native boolean nativeClassInit(); // Initialize native
														// class:
	// cache Method IDs for
	// callbacks

	protected native void nativeSurfaceInit(Object surface); // A new surface is
	// available

	protected native void nativeSurfaceFinalize(); // Surface about to be
	// destroyed

	private long native_custom_data; // Native code will use this to keep
	// private data

	private int position; // Current position, reported by native code
	private int duration; // Current clip duration, reported by native code

	// Called from native code. This sets the content of the TextView from the
	// UI thread.
	protected void setMessage(final String message) {
		Log.d(TAG, message);
	}

	// Called from native code. Native code calls this once it has created its
	// pipeline and
	// the main loop is running, so it is ready to accept commands.
	protected void onGStreamerInitialized() {

	}

	// Called from native code
	protected void setCurrentPosition(final int position, final int duration) {

	}

	// Called from native code when the size of the media changes or is first
	// detected.
	// Inform the video surface about the new size and recalculate the layout.
	protected void onMediaSizeChanged(int width, int height) {

	}

	public int getPosition() {
		return position;
	}

	public int getDuration() {
		return duration;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

}
