package by.gravity.doublexplayer.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import by.gravity.doubleplayer.core.fragment.BaseVideoFragment;
import by.gravity.doublexplayer.R;
import by.gravity.doublexplayer.activity.MainActivity;
import by.gravity.doublexplayer.model.Rate;
import by.gravity.doublexplayer.model.VideoState;

public class VideoFragment extends BaseVideoFragment {

	private Rate mRate = Rate.X1;

	private static final String ARG_VIDEO_STATE = "ARG_VIDEO_STATE";

	private static final String DEFAULT_MEDIA_URI = "file://"
			+ Environment.getExternalStorageDirectory().getAbsolutePath()
			+ "/DoublePlayer/Video/1.mp4";

	private Button mPlayButton;

	public static VideoFragment newInstance() {

		VideoFragment fragment = new VideoFragment();
		Bundle bundle = new Bundle();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		init(DEFAULT_MEDIA_URI);

	}

	public void init(String uri) {
		setMediaUri(uri);
		setRate(Rate.X1);
		initUI();

	}

	private void init() {

		final VideoState videoState = getDefaultVideoState();
		if (videoState != null) {
			setMediaUri(videoState.getMediaUri());
			setRate(videoState.getRate());
			setCurrentPosition(videoState.getPosition());
			initUI();

		}
	}

	private void initUI() {

		mPlayButton = (Button) getView().findViewById(R.id.playButton);
		mPlayButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (isPlayed()) {
					pause();
				} else {
					play();
				}
			}
		});

		TextView rateButton = (TextView) getView()
				.findViewById(R.id.rateButton);
		rateButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {

				Rate rate = getRate().getNext(getRate());
				setRate(rate);
			}
		});

		Button fullScreenButton = (Button) getView().findViewById(
				R.id.fullScreenButton);
		fullScreenButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				boolean isPlayed = isPlayed();
				pause();
				onFullScreenClick();
				if (isPlayed) {
					play();
				}
			}
		});

		Button nextFrameButton = (Button) getView().findViewById(
				R.id.nextFrameButton);
		nextFrameButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isPlayed()) {
					onNextFrameClick();
				}
			}
		});

		Button prevFrameButton = (Button) getView().findViewById(
				R.id.prevFrameButton);
		prevFrameButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				onPrevFrameClick();

			}
		});

		showProgressBar();

	}

	private void showProgressBar() {
		LinearLayout progressBar = (LinearLayout) getView().findViewById(
				R.id.progressBar);
		if (progressBar != null) {
			progressBar.setVisibility(View.VISIBLE);
		}
	}

	private void onNextFrameClick() {
		Log.e("test", "next Frame");
		nativeNextFrame();
	}

	private void onPrevFrameClick() {
		Log.e("test", "prev Frame");
		nativePrevFrame();
	}

	private void onFullScreenClick() {

		((MainActivity) getActivity()).onFullScreenClick(getTag());

	}

	public VideoState createVideoState() {

		return new VideoState(getMediaUri(), getPosition(), getRate(),
				isPlayed());
	}

	private VideoState getVideoState() {

		return (VideoState) getArguments().getSerializable(ARG_VIDEO_STATE);
	}

	private static VideoState getDefaultVideoState() {

		return new VideoState(DEFAULT_MEDIA_URI, 5000, Rate.X1, false);
	}

	private Rate getRate() {

		return mRate;
	}

	public void setRate(Rate rate) {

		mRate = rate;
		setRateUI(rate);
		setRate(rate.getValue());
	}

	private void setRateUI(Rate rate) {

		TextView rateButton = (TextView) getView()
				.findViewById(R.id.rateButton);
		rateButton.setText(rate.getName());
	}

	@Override
	public void play() {

		super.play();
		setPlayPauseUI();
	}

	@Override
	public void pause() {

		super.pause();
		setPlayPauseUI();
	}

	private void setPlayPauseUI() {

		if (isPlayed()) {
			mPlayButton.setBackgroundResource(R.drawable.btn_pause);
		} else {
			mPlayButton.setBackgroundResource(R.drawable.btn_play);
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
	public int getSeekBarID() {

		return R.id.seekBar;
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
