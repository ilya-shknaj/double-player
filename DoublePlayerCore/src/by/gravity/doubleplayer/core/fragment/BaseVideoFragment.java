package by.gravity.doubleplayer.core.fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import by.gravity.common.utils.StringUtil;
import by.gravity.doubleplayer.core.IPlayer;
import by.gravity.doubleplayer.core.view.GStreamerSurfaceView;
import by.gravity.doublexplayer.R;
import by.gravity.doublexplayer.model.Rate;
import by.gravity.doublexplayer.widget.RangeSeekBar;
import by.gravity.doublexplayer.widget.RangeSeekBar.OnRangeSeekBarChangeListener;

import com.gstreamer.GStreamer;

abstract public class BaseVideoFragment extends NativeVideoFragment implements
		SurfaceHolder.Callback, OnSeekBarChangeListener, IPlayer {

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

	abstract public int getCurrentPositionTextViewID();

	abstract public int getTotaTextViewID();

	private boolean preRateStatePlaying = false;

	private Rate mRate = Rate.X1;

	private Handler handler;

	private Runnable mSetRateRunnable;

	private Runnable mPlayRunnable;

	private Runnable mSetPositionRunnable;

	private static final long RUNNABLE_DELAY = 200;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		gstreamerInit();
		handler = new Handler();

	}

	private void gstreamerInit() {

		try {
			GStreamer.init(getActivity());
		} catch (Exception e) {
			Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG)
					.show();
			getActivity().finish();
			return;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		PowerManager pm = (PowerManager) getActivity().getSystemService(
				Context.POWER_SERVICE);
		wake_lock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);
		wake_lock.setReferenceCounted(false);

		SurfaceView sv = (SurfaceView) getView().findViewById(getSurfaceID());
		SurfaceHolder sh = sv.getHolder();
		sh.addCallback(this);

		RangeSeekBar sb = new RangeSeekBar(getActivity());
		sb.setTag(RangeSeekBar.TAG);
		sb.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener() {

			@Override
			public void onRangeSeekBarValuesChanged(RangeSeekBar bar,
					Integer currentValue) {
				desired_position = currentValue;
				// If this is a local file, allow scrub seeking, this is, seek
				// as soon
				// as the slider is moved.
				if (is_local_media) {
					nativeSetPosition(desired_position);
				}
				updateTimeWidget();
			}
		});

		ViewGroup layout = (ViewGroup) getView().findViewById(R.id.seekBar);

		layout.addView(sb);

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(getViewID(), null);
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
	}

	@Override
	public void onDestroyView() {

		// nativeFinalize();
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

	public boolean isPlayed() {

		return is_playing_desired;
	}

	// The text widget acts as an slave for the seek bar, so it reflects what
	// the seek bar shows, whether
	// it is an actual pipeline position or the position the user is currently
	// dragging to.
	@SuppressWarnings("unchecked")
	private void updateTimeWidget() {

		TextView tv = (TextView) getView().findViewById(
				getCurrentPositionTextViewID());
		TextView duration = (TextView) getView().findViewById(
				getTotaTextViewID());
		RangeSeekBar sb = getRangeSeekBar();
		if (tv == null || duration == null || sb == null) {
			return;
		}
		int pos = sb.getSelectedCurrentValue();
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		tv.setText(df.format(new Date(pos)));
		duration.setText(df.format(new Date(getDuration())));
	}

	// Called from native code
	@Override
	protected void setCurrentPosition(final int position, final int duration) {
		final RangeSeekBar sb = getRangeSeekBar();
		// Log.e(TAG, "position = " + position + " duration " + duration);
		// Ignore position messages from the pipeline if the seek bar is being
		// dragged
		if (sb.isPressed()) {
			return;
		}

		getActivity().runOnUiThread(new Runnable() {

			public void run() {

				sb.setAbsoluteMaxValue(duration);
				sb.setCurrentValue(position);
				updateTimeWidget();
				sb.setEnabled(duration != 0);
			}
		});
		setCurrentPosition(position);
		setDuration(duration);
	}

	static {
		System.loadLibrary("gstreamer_android");
		System.loadLibrary("doublePlayer");
		nativeClassInit();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		Log.d("GStreamer", "Surface changed to format " + format + " width "
				+ width + " height " + height);
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
		final GStreamerSurfaceView gsv = (GStreamerSurfaceView) getView()
				.findViewById(getSurfaceID());
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
	@Override
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
		if (!is_local_media) {
			nativeSetPosition(desired_position);
		}
		if (is_playing_desired) {
			nativePlay();
		}
	}

	@Override
	public void setRate(double rate) {

		nativeSetRate(rate);

	}

	@Override
	public void play() {
		Log.i(TAG, "start play");
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

	@Override
	protected void onVideoFinished() {
		super.onVideoFinished();
		Log.e(TAG, "onVideoFinished");
		Log.e(TAG, "is_playing_desired " + is_playing_desired
				+ " isRepeatMode " + isRepeatMode);
		if (is_playing_desired && isRepeatMode) {
			// postDelayedSetRate();
			setVideoFragment();
			postDelayedPlay();
		} else {
			is_playing_desired = false;
			Runnable runnable = new Runnable() {

				@Override
				public void run() {
					updatePlayPauseUI();
				}
			};

			postDelayed(runnable, RUNNABLE_DELAY);

		}
	}

	@Override
	protected void onSetRateFinished() {
		super.onSetRateFinished();
		if (isPreRateStatePlaying()) {
			setPreRateStatePlaying(false);
			postDelayedPlay();
		}
	}

	private void postDelayedSetRate() {
		if (mSetRateRunnable == null) {
			mSetRateRunnable = new Runnable() {

				@Override
				public void run() {
					setRate(getRate());
				}
			};
		}

		postDelayed(mSetRateRunnable, RUNNABLE_DELAY);

	}

	protected void postDelayedSetPosition(final int position, final boolean play) {
		if (mSetPositionRunnable == null) {
			mSetPositionRunnable = new Runnable() {

				@Override
				public void run() {
					Log.e(TAG, "postDelayedSetPosition to " + position);
					setPosition(position);

				}
			};
		}

		postDelayed(mSetPositionRunnable, 300);
	}

	protected void postDelayedPlay() {
		if (mPlayRunnable == null) {
			mPlayRunnable = new Runnable() {

				@Override
				public void run() {
					play();
				}
			};
		}

		postDelayed(mPlayRunnable, RUNNABLE_DELAY);

	}

	private void postDelayed(Runnable runnable, long time) {
		handler.postDelayed(runnable, time);
	}

	protected void updatePlayPauseUI() {

	}

	public int getDesiredPosition() {

		return desired_position;
	}

	public String getMediaUri() {

		return mediaUri;
	}

	protected Rate getRate() {

		return mRate;
	}

	protected void setRate(Rate rate) {
		mRate = rate;
		Log.e(TAG, "setRate isPlayed()=" + isPlayed());
		setPreRateStatePlaying(isPlayed());
		setRate(rate.getValue());
		setRateUI(rate);

	}

	protected void setRateUI(Rate rate) {

		TextView rateButton = (TextView) getView()
				.findViewById(R.id.rateButton);
		rateButton.setText(rate.getName());
	}

	protected void setVideoFragment() {
		RangeSeekBar seekBar = getRangeSeekBar();
		int minValue = seekBar.hasMinValue() ? seekBar.getSelectedMinValue()
				: seekBar.getAbsoluteMinValue();
		int maxValue = seekBar.hasMaxValue() ? seekBar.getSelectedMaxValue()
				: seekBar.getAbsoluteMaxValue();

		nativeSetFragment(minValue, maxValue);

	}

	private boolean isRepeatMode = false;

	public void setRepeatMode(boolean isRepeat) {
		this.isRepeatMode = isRepeat;
		nativeSetRepeatMode(isRepeat);
	}

	public boolean getRepeatMode() {
		return isRepeatMode;
	}

	public boolean isPreRateStatePlaying() {
		return preRateStatePlaying;
	}

	public void setPreRateStatePlaying(boolean preRateStatePlaying) {
		this.preRateStatePlaying = preRateStatePlaying;
	}

	protected RangeSeekBar getRangeSeekBar() {
		return (RangeSeekBar) getView().findViewWithTag(RangeSeekBar.TAG);
	}

}
