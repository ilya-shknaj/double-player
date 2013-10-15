package by.gravity.doubleplayer.core.fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
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
import by.gravity.common.Constants;
import by.gravity.common.utils.StringUtil;
import by.gravity.doubleplayer.core.IPlayer;
import by.gravity.doubleplayer.core.view.GStreamerSurfaceView;
import by.gravity.doublexplayer.R;
import by.gravity.doublexplayer.model.Rate;
import by.gravity.doublexplayer.widget.RangeSeekBar;
import by.gravity.doublexplayer.widget.RangeSeekBar.OnRangeSeekBarChangeListener;

import com.gstreamer.GStreamer;

abstract public class BaseVideoFragment extends NativeVideoFragment implements SurfaceHolder.Callback, OnSeekBarChangeListener, IPlayer {

	abstract public int getSurfaceID();

	abstract public int getViewID();

	abstract public int getCurrentPositionTextViewID();

	abstract public int getTotaTextViewID();

	private static final String TAG = BaseVideoFragment.class.getSimpleName();

	private static final String DATE_FORMAT = "HH:mm:ss";

	private static final String UTC = "UTC";

	private boolean isLocalMedia;
	private int desiredPosition;
	private String mediaUriString;

	private boolean isPlaying;

	private PowerManager.WakeLock wakeLock;

	private boolean preRateStatePlaying = false;

	private Rate rate = Rate.X1;

	private final Handler handler;

	private static final long RUNNABLE_DELAY = 350;

	private boolean isRepeatMode = false;

	private final SimpleDateFormat dateFormat;

	public BaseVideoFragment() {
		dateFormat = new SimpleDateFormat(DATE_FORMAT);
		dateFormat.setTimeZone(TimeZone.getTimeZone(UTC));
		handler = new Handler();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		initWakeLock();
		initSurfaceView();
		initSeekBar();
		restoreState(savedInstanceState);
		init();

	}

	@SuppressWarnings("deprecation")
	private void initWakeLock() {
		PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
		WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);
		wakeLock.setReferenceCounted(false);
		setWakeLock(wakeLock);
	}

	private void initSurfaceView() {
		SurfaceView sv = (SurfaceView) getView().findViewById(getSurfaceID());
		SurfaceHolder sh = sv.getHolder();
		sh.addCallback(this);
	}

	private void initSeekBar() {
		RangeSeekBar sb = new RangeSeekBar(getActivity());
		sb.setTag(RangeSeekBar.TAG);
		sb.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener() {

			@Override
			public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Integer currentValue) {
				setDesiredPosition(currentValue);
				if (isLocalMedia()) {
					nativeSetPosition(getDesiredPosition());
				}
				updateTimeWidget();
			}
		});

		ViewGroup layout = (ViewGroup) getView().findViewById(R.id.seekBar);
		layout.removeAllViews();

		layout.addView(sb);

	}

	private void restoreState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			setPlaying(savedInstanceState.getBoolean("playing"));
			setPosition(savedInstanceState.getInt("position"));
			setDuration(savedInstanceState.getInt("duration"));
			setMediaUri(savedInstanceState.getString("mediaUri"));
		} else {
			setPlaying(false);
			setPosition(0);
			setDuration(0);

		}
	}

	private void init() {
		setLocalMedia(false);

		gstreamerInit();
		nativeInit();
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		return inflater.inflate(getViewID(), null);
	}

	@Override
	public void onDestroyView() {

		// nativeFinalize();
		if (getWakeLock().isHeld()) {
			getWakeLock().release();
		}
		super.onDestroy();

	}

	// Called from native code
	@Override
	protected void onGStreamerInitialized() {

		Log.i("GStreamer", "GStreamer initialized:");

		setMediaUri(getMediaUriString());
		setPosition(getPosition());
		if (isPlaying()) {
			playPause();
		} else {
			pause();
		}

	}

	private void updateTimeWidget() {
		View tv = getView().findViewById(getCurrentPositionTextViewID());
		View duration = getView().findViewById(getTotaTextViewID());
		RangeSeekBar sb = getRangeSeekBar();
		if (tv == null || duration == null || sb == null) {
			return;
		}

		((TextView) tv).setText(getDateFormat().format(new Date(sb.getSelectedCurrentValue())));
		((TextView) duration).setText(getDateFormat().format(new Date(getDuration())));
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

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

		Log.e(TAG, "Surface changed to format " + format + " width " + width + " height " + height);
		nativeSurfaceInit(holder.getSurface());
	}

	public void surfaceCreated(SurfaceHolder holder) {

		Log.d(TAG, "Surface created: " + holder.getSurface());
	}

	public void surfaceDestroyed(SurfaceHolder holder) {

		Log.e(TAG, "Surface destroyed");
		nativeSurfaceFinalize();
	}

	// Called from native code
	@Override
	protected void onMediaSizeChanged(int width, int height) {

		Log.i(TAG, "Media size changed to " + width + "x" + height);
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
	@Override
	public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
		if (fromUser == false) {
			return;
		}
		setDesiredPosition(progress);
		// If this is a local file, allow scrub seeking, this is, seek as soon
		// as the slider is moved.
		if (isLocalMedia()) {
			nativeSetPosition(getDesiredPosition());
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
		// smoothly enough.Therefore, perform only the seek when the slider is
		// released.
		if (!isLocalMedia()) {
			nativeSetPosition(getDesiredPosition());
		}
		if (isPlaying()) {
			nativePlay();
		}
	}

	public void setRate(double rate) {

		nativeSetRate(rate);

	}

	public void playPause() {
		Log.i(TAG, "playPause call");
		if (isPlaying()) {
			pause();
		} else {
			play();
		}
	}

	public void play() {
		Log.e(TAG, "play");
		setPlaying(true);
		getWakeLock().acquire();
		nativePlay();
	}

	public void pause() {
		Log.e(TAG, "pause");
		setPlaying(false);
		getWakeLock().release();
		nativePause();

	}

	public void setMediaUri(String uri) {

		if (!StringUtil.isEmpty(uri)) {
			setMediaUriString(uri);
			nativeSetUri(uri);
			setLocalMedia(uri.startsWith(Constants.FILE));
		}

	}

	@Override
	protected void onVideoFinished() {
		super.onVideoFinished();
		Log.e(TAG, "onVideoFinished");
		Log.e(TAG, "is_playing_desired " + isPlaying + " isRepeatMode " + isRepeatMode);
		if (isPlaying() && isRepeatMode()) {
			setVideoFragment();
			setPlaying(false);
			postDelayedPlay();
		} else {
			setVideoFragment();
			setPlaying(false);
			postDelayedUpdatePlayPauseUI();

		}
	}

	@Override
	protected void onSetRateFinished() {
		super.onSetRateFinished();
		Log.e(TAG, "onSetRateFinished");
		if (isPreRateStatePlaying()) {
			setPreRateStatePlaying(false);
			setPlaying(false);
			postDelayedPlay();
		}
	}

	private void postDelayedSetRate() {
		postDelayed(new Runnable() {

			@Override
			public void run() {
				setRate(getRate());
			}
		}, RUNNABLE_DELAY);

	}

	private void postDelayedUpdatePlayPauseUI() {
		postDelayed(new Runnable() {

			@Override
			public void run() {
				updatePlayPauseUI();
			}
		}, RUNNABLE_DELAY);
	}

	protected void postDelayedSetPosition(final int position, final boolean isPlayed) {

		postDelayed(new Runnable() {

			@Override
			public void run() {
				Log.e(TAG, "postDelayedSetPosition to " + position);
				setPosition(position);
				if (isPlayed) {
					playPause();
				}

			}
		}, RUNNABLE_DELAY);
	}

	protected void postDelayedPlay() {

		postDelayed(new Runnable() {

			@Override
			public void run() {
				playPause();
			}
		}, RUNNABLE_DELAY);

	}

	private void postDelayed(Runnable runnable, long time) {
		handler.postDelayed(runnable, time);
	}

	protected void updatePlayPauseUI() {

	}

	public int getDesiredPosition() {

		return desiredPosition;
	}

	public void setMediaUriString(String mediaUriString) {
		this.mediaUriString = mediaUriString;
	}

	public String getMediaUriString() {

		return mediaUriString;
	}

	protected Rate getRate() {

		return this.rate;
	}

	public void setRate(Rate rate) {
		this.rate = rate;
		Log.e(TAG, "setRate isPlayed()=" + isPlaying());
		setPreRateStatePlaying(isPlaying());
		setRate(rate.getValue());
		setRateUI(rate);

	}

	protected void setRateUI(Rate rate) {

		TextView rateButton = (TextView) getView().findViewById(R.id.rateButton);
		rateButton.setText(rate.getName());
	}

	protected void setVideoFragment() {
		RangeSeekBar seekBar = getRangeSeekBar();
		int minValue = seekBar.hasMinValue() ? seekBar.getSelectedMinValue() : seekBar.getAbsoluteMinValue();
		int maxValue = seekBar.hasMaxValue() ? seekBar.getSelectedMaxValue() : seekBar.getAbsoluteMaxValue();

		nativeSetFragment(minValue, maxValue);

	}

	private void setDesiredPosition(int position) {
		desiredPosition = position;
	}

	public boolean isLocalMedia() {
		return isLocalMedia;
	}

	public void setLocalMedia(boolean isLocalMedia) {
		this.isLocalMedia = isLocalMedia;
	}

	public PowerManager.WakeLock getWakeLock() {
		return wakeLock;
	}

	public void setWakeLock(PowerManager.WakeLock wakeLock) {
		this.wakeLock = wakeLock;
	}

	public boolean isPlaying() {

		return isPlaying;
	}

	protected void setPlaying(boolean isPlaying) {
		this.isPlaying = isPlaying;
	}

	public void setRepeatMode(boolean isRepeat) {
		this.isRepeatMode = isRepeat;
		// nativeSetRepeatMode(isRepeat);
	}

	public boolean isRepeatMode() {
		return isRepeatMode;
	}

	public Handler getHandler() {
		return handler;
	}

	private SimpleDateFormat getDateFormat() {
		return dateFormat;
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

	static {
		System.load("/data/data/by.gravity.doublexplayer/lib/libgstreamer_android.so");
		System.load("/data/data/by.gravity.doublexplayer/lib/libdoublePlayer.so");
		nativeClassInit();
	}

}
