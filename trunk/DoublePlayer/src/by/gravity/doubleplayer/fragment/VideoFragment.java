package by.gravity.doubleplayer.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import by.gravity.doubleplayer.R;
import by.gravity.doubleplayer.core.fragment.BaseVideoFragment;

public class VideoFragment extends BaseVideoFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setMediaUri("file://" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/DoublePlayer/Video/1.mp4");

		ImageButton playButton = (ImageButton) getView().findViewById(R.id.play);
		playButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				play();
			}
		});

//		ImageButton stopButton = (ImageButton) getView().findViewById(R.id.play);
//		stopButton.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				pause();
//			}
//		});

	}

	@Override
	public int getSurfaceID() {
		return R.id.surface_video;
	}

	@Override
	public int getViewID() {
		return R.layout.f_video;
	}

}
