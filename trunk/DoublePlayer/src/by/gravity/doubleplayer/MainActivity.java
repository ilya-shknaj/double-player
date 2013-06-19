package by.gravity.doubleplayer;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;
import by.gravity.common.utils.StringUtil;
import by.gravity.doubleplayer.manager.SettingsManager;

public class MainActivity extends Activity {

	private static final int LEFT_OPEN_REQUEST_CODE = 100;

	private static final int LEFT_CAMERA_REQUEST_CODE = 101;

	private static final int RIGHT_OPEN_REQUEST_CODE = 200;

	private static final int RIGHT_CAMERA_REQUEST_CODE = 201;

	private VideoView mLeftVideoView;

	private VideoView mRightVideoView;

	private MediaController mMediaControllerLeft;

	private MediaController mMediaControllerRight;

	private ImageView leftOpenButton;

	private ImageView leftCameraButton;

	private ImageView rightOpenButton;

	private ImageView rightCameraButton;

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
				openFileAction(LEFT_OPEN_REQUEST_CODE, SettingsManager.getLeftPath());
			}
		});

		leftCameraButton = (ImageView) findViewById(R.id.action_bar_left).findViewById(R.id.camera);
		leftCameraButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openCameraAction(LEFT_CAMERA_REQUEST_CODE, SettingsManager.getLeftPath());
			}
		});

		rightOpenButton = (ImageView) findViewById(R.id.action_bar_right).findViewById(R.id.open);
		rightOpenButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openFileAction(RIGHT_OPEN_REQUEST_CODE, SettingsManager.getRightPath());
			}
		});

		rightCameraButton = (ImageView) findViewById(R.id.action_bar_right).findViewById(R.id.camera);
		rightCameraButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openCameraAction(RIGHT_CAMERA_REQUEST_CODE, SettingsManager.getRightPath());
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

	@Override
	protected void onResume() {
		super.onResume();
		if (mLeftVideoView != null) {
			mLeftVideoView.start();
		}
		if (mRightVideoView != null) {
			mRightVideoView.start();
		}
	}

	private void openFileAction(int requestCode, String defaultPath) {
		Intent intent = new Intent("org.openintents.action.PICK_FILE");
		if (!StringUtil.isEmpty(defaultPath)) {
			intent.setData(Uri.parse(defaultPath));
		}
		startActivityForResult(intent, requestCode);
	}

	private void openCameraAction(int requestCode, String defaultPath) {
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		Uri uri = Uri.parse(defaultPath + "/" + generateVideoName());
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		startActivityForResult(intent, requestCode);
	}

	private String generateVideoName() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		return dateFormat.format(System.currentTimeMillis()) + ".mp4";

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) {
			return;
		}

		VideoView videoView = null;
		MediaController controller = null;
		if (requestCode / 100 == 1) {
			videoView = mLeftVideoView;
			controller = mMediaControllerLeft;
		} else if (requestCode / 100 == 2) {
			videoView = mRightVideoView;
			controller = mMediaControllerRight;
		}
		if (videoView != null && controller != null) {
			initVideo(videoView, controller, data.getData());
		}

	}

	private void initVideo(VideoView videoView, MediaController mediaController, Uri uri) {
		videoView.setVisibility(View.VISIBLE);
		videoView.setVideoURI(uri);
		videoView.setMediaController(mediaController);
		videoView.start();
	}

}
