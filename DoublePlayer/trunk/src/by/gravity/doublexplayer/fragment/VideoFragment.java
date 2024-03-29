package by.gravity.doublexplayer.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import by.gravity.common.Constants;
import by.gravity.doublexplayer.R;
import by.gravity.doublexplayer.activity.MainActivity;
import by.gravity.doublexplayer.core.fragment.BaseVideoFragment;
import by.gravity.doublexplayer.googleanalytics.SettingActivityTracking;
import by.gravity.doublexplayer.googleanalytics.VideoFragmentTracking;
import by.gravity.doublexplayer.model.Rate;
import by.gravity.doublexplayer.model.VideoState;

public class VideoFragment extends BaseVideoFragment {

	private static final String TAG = VideoFragment.class.getSimpleName();

	private static final String ARG_MEDIA_URI = "ARG_MEDIA_URI";

	private static final String DEFAULT_MEDIA_URI = Constants.FILE + Environment.getExternalStorageDirectory().getAbsolutePath()
			+ "/DoublePlayer/Video/624.mp4";

	// + "/DCIM/Camera/VID_20131005_123558.mp4";

	public static VideoFragment newInstance(String mediaUri) {

		VideoFragment fragment = new VideoFragment();
		Bundle bundle = new Bundle();
		bundle.putString(ARG_MEDIA_URI, mediaUri);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		init(getArguments().getString(ARG_MEDIA_URI));

	}

	public void init(String uri) {
		if (uri == null) {
			uri = DEFAULT_MEDIA_URI;
		}
		setMediaUri(uri);
		setRate(Rate.X1);
		setRepeatMode(true);
		if (uri != null) {
			initUI();
		}
		postDelayedPlay();

	}

	private void init() {

		final VideoState videoState = getDefaultVideoState();
		if (videoState != null) {
			setMediaUri(videoState.getMediaUri());
			setRate(videoState.getRate());
			setCurrentPosition(videoState.getPosition());
			setRepeatMode(true);
			initUI();

		}
	}

	private void initUI() {

		View playButton = getView().findViewById(R.id.playButton);
		playButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (isPlaying()) {
					pause();
				} else {
					playPause();
					VideoFragmentTracking.trackPlayPause();
				}
			}
		});

		View rateButton = getView().findViewById(R.id.rateButton);
		rateButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {

				Rate rate = getRate().getNext(getRate());
				setRate(rate);
				VideoFragmentTracking.trackSetRate(rate.getValue());
			}
		});

		View fullScreenButton = getView().findViewById(R.id.fullScreenButton);
		fullScreenButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				boolean isPlayed = isPlaying();
				pause();
				onFullScreenClick();
				postDelayedSetPosition(getPosition(), isPlayed);
				VideoFragmentTracking.trackFullScreen();
			}
		});

		View nextFrameButton = getView().findViewById(R.id.nextFrameButton);
		nextFrameButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isPlaying()) {
					nextFrame();
					VideoFragmentTracking.trackNextFrame();
				}
			}
		});

		View prevFrameButton = getView().findViewById(R.id.prevFrameButton);
		prevFrameButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (!isPlaying()) {
					prevFrame();
					VideoFragmentTracking.trackPrevFrame();
				}

			}
		});

		View repeatMode = getView().findViewById(R.id.repeatButton);
		repeatMode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String message = !isRepeatMode() ? getString(R.string.repeat_enabled) : getString(R.string.repeat_disabled);
				setRepeatMode(!isRepeatMode());
				VideoFragmentTracking.trackRepeatMode();
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
			}
		});

		View leftFragment = getView().findViewById(R.id.leftFragmentLayout);
		leftFragment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				Log.d(TAG, "leftFragmentClick");
				if (!getRangeSeekBar().hasMinValue()) {
					getRangeSeekBar().setSelectedMinValue(getPosition());
					getRangeSeekBar().setHasMinValue(true);
					setVideoFragment();
				} else {
					getRangeSeekBar().setHasMinValue(false);
				}

				VideoFragmentTracking.trackLeftFragment();
				updateVideoFragmentUI(FragmentButton.START);

			}
		});

		View rightFragment = getView().findViewById(R.id.rightFragmentLayout);
		rightFragment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				Log.d(TAG, "rightFragmentClick");
				int minPosition = getRangeSeekBar().getSelectedMinValue();
				getRangeSeekBar().setDeliveredMinValue(getPosition());
				boolean hasMinValue = getRangeSeekBar().hasMinValue();
				getRangeSeekBar().setHasMinValue(true);
				if (!getRangeSeekBar().hasMaxValue()) {
					getRangeSeekBar().setSelectedMaxValue(getPosition());
					getRangeSeekBar().setHasMaxValue(true);
					setVideoFragment();

				} else {
					getRangeSeekBar().setHasMaxValue(false);
					setVideoFragment();
				}
				getRangeSeekBar().setHasMinValue(hasMinValue);
				getRangeSeekBar().setSelectedMinValue(minPosition);
				VideoFragmentTracking.trackRightFragment();
				updateVideoFragmentUI(FragmentButton.FINISH);

			}
		});

		View nextFile = getView().findViewById(R.id.nextFileButton);
		if (nextFile != null) {
			nextFile.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View paramView) {
					((MainActivity) getActivity()).playFileInFolder(getTag(), getArguments().getString(ARG_MEDIA_URI), true);
					VideoFragmentTracking.trackNextFile();
				}
			});
		}

		View prevFile = getView().findViewById(R.id.prevFileButton);
		if (prevFile != null) {
			prevFile.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View paramView) {
					((MainActivity) getActivity()).playFileInFolder(getTag(), getArguments().getString(ARG_MEDIA_URI), false);
					VideoFragmentTracking.trackPrevFile();
				}
			});
		}

		TextView fileName = (TextView) getView().findViewById(R.id.fileName);
		String mediaUri = getArguments().getString(ARG_MEDIA_URI);
		int indexSlash = mediaUri.lastIndexOf("/");
		int indexDot = mediaUri.lastIndexOf(".");
		if (indexDot == -1) {
			indexDot = mediaUri.length();
		}
		fileName.setText(mediaUri.substring(indexSlash + 1, indexDot));

		showProgressBar();

	}

	@Override
	public void onResume() {
		super.onResume();
		postDelayedSetPosition(getPosition(), isPlaying());
	}

	private void showProgressBar() {
		LinearLayout progressBar = (LinearLayout) getView().findViewById(R.id.progressBar);
		if (progressBar != null) {
			progressBar.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void nextFrame() {
		Log.e("test", "next Frame");
		nativeNextFrame();
	}

	@Override
	public void prevFrame() {
		int nextPosition = (getPosition() - 40);
		nativeSeekToPosition(nextPosition);
	}

	private void onFullScreenClick() {
		((MainActivity) getActivity()).onFullScreenClick(getTag());

	}

	public VideoState createVideoState() {

		return new VideoState(getMediaUriString(), getPosition(), getRate(), isPlaying());
	}

	private static VideoState getDefaultVideoState() {

		return new VideoState(DEFAULT_MEDIA_URI, 5000, Rate.X1, false);
	}

	@Override
	public void playPause() {

		super.playPause();
		updatePlayPauseUI();

	}

	@Override
	public void playPause(boolean isPlaying) {
		setPlaying(!isPlaying);
		playPause();
	}

	@Override
	public void pause() {

		super.pause();
		updatePlayPauseUI();
	}

	@Override
	protected void updatePlayPauseUI() {
		View view = getView();
		if (view == null) {
			return;
		}
		View playButton = view.findViewById(R.id.playButton);
		if (playButton == null) {
			return;
		}

		if (isPlaying()) {
			Log.d(TAG, "set pause button image");
			playButton.setBackgroundResource(R.drawable.btn_pause);
		} else {
			Log.d(TAG, "set play button image");
			playButton.setBackgroundResource(R.drawable.btn_play);
		}

	}

	private enum FragmentButton {
		START, FINISH
	}

	@SuppressWarnings("deprecation")
	protected void updateVideoFragmentUI(FragmentButton fragmentButton) {
		View button;
		if (fragmentButton == FragmentButton.START) {
			button = getView().findViewById(R.id.leftFragmentButton);
			if (getRangeSeekBar().hasMinValue()) {
				button.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_remove_left_position));
			} else {
				button.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_add_left_position));
			}
		} else {
			button = getView().findViewById(R.id.rightFragmentButton);
			if (getRangeSeekBar().hasMaxValue()) {
				button.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_remove_right_position));
			} else {
				button.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_add_right_position));
			}
		}

	}

	@Override
	public int getSurfaceID() {

		return R.id.surface_video;
	}

	@Override
	public int getViewID() {

		return R.layout.f_video;
	}

	@Override
	public int getCurrentPositionTextViewID() {

		return R.id.currentTime;
	}

	@Override
	public int getTotaTextViewID() {

		return R.id.totalTime;
	}

}
