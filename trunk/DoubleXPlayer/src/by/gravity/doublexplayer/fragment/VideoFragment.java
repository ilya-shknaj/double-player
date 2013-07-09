package by.gravity.doublexplayer.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import by.gravity.doubleplayer.core.fragment.BaseVideoFragment;
import by.gravity.doublexplayer.R;
import by.gravity.doublexplayer.activity.MainActivity;
import by.gravity.doublexplayer.model.Rate;
import by.gravity.doublexplayer.model.VideoState;

public class VideoFragment extends BaseVideoFragment {

	private Rate mRate = Rate.X1;

	private static final String ARG_VIDEO_STATE = "ARG_VIDEO_STATE";

	private static final String DEFAULT_MEDIA_URI = "file://" + Environment.getExternalStorageDirectory().getAbsolutePath()
			+ "/DoublePlayer/Video/1.mp4";

	public static VideoFragment newInstance(VideoState videoState) {

		VideoFragment fragment = new VideoFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(ARG_VIDEO_STATE, videoState != null ? videoState : getDefaultVideoState());
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);

		Button playButton = (Button) getView().findViewById(R.id.playButton);
		playButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (isPlayed()) {
					pause();
				} else {
					play();
				}
			}
		});

		Button rateButton = (Button) getView().findViewById(R.id.rateButton);
		rateButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {

				Rate rate = getRate().getNext(getRate());
				setRate(rate);
			}
		});

		Button fullScreenButton = (Button) getView().findViewById(R.id.fullScreenButton);
		fullScreenButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {

				onFullScreenClick();
			}
		});

		init();

	}

	private void init() {

		final VideoState videoState = getVideoState();
		if (videoState != null) {
			setMediaUri(videoState.getMediaUri());
			setPosition(videoState.getPosition());

			if (videoState.isPlayed()) {
				play();
			} else {
				pause();
			}
			setRate(videoState.getRate());

		}
	}

	private void onFullScreenClick() {

		((MainActivity) getActivity()).showFullScreen(createVideoState());
	}

	private VideoState createVideoState() {

		return new VideoState(getMediaUri(), getPosition(), getRate(), isPlayed());
	}

	private VideoState getVideoState() {

		return (VideoState) getArguments().getSerializable(ARG_VIDEO_STATE);
	}

	private static VideoState getDefaultVideoState() {

		return new VideoState(DEFAULT_MEDIA_URI, 0, Rate.X4, false);
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

		Button rateButton = (Button) getView().findViewById(R.id.rateButton);
		rateButton.setText(rate.getName());
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
