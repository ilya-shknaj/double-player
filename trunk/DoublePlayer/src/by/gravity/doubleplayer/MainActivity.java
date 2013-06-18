package by.gravity.doubleplayer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private static final int RESULT_LEFT_VIDEO_CODE = 345;

	private static final int RESULT_RIGHT_VIDEO_CODE = 346;

	private static final String PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Video/";

	private VideoView mLeftVideoView;

	private VideoView mRightVideoView;

	private MediaController mMediaControllerLeft;

	private MediaController mMediaControllerRight;

	private ImageView leftOpenButton;

	private ImageView rightOpenButton;

	private ImageView settingsButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_main);

		mLeftVideoView = (VideoView) findViewById(R.id.leftVideo);
		mRightVideoView = (VideoView) findViewById(R.id.rightVideoUp);

		mMediaControllerLeft = new MediaController(this, false);
		mMediaControllerRight = new MediaController(this, false);

		leftOpenButton = (ImageView) findViewById(R.id.action_bar_left).findViewById(R.id.open);

		leftOpenButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
			}
		});

		rightOpenButton = (ImageView) findViewById(R.id.actio_bar_right).findViewById(R.id.open);
		rightOpenButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

			}
		});

		settingsButton = (ImageView) findViewById(R.id.settings);
		settingsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
				startActivity(intent);
			}
		});
	}

	private void initVideo(VideoView videoView, MediaController mediaController, Uri uri, Button button) {
		button.setVisibility(View.GONE);
		videoView.setVisibility(View.VISIBLE);
		videoView.setVideoURI(uri);
		videoView.setMediaController(mediaController);
		videoView.start();
	}

}
