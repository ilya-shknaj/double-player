package by.gravity.doublexplayer.activity;

import java.text.SimpleDateFormat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import by.gravity.common.utils.FileUtil;
import by.gravity.common.utils.StringUtil;
import by.gravity.doublexplayer.R;
import by.gravity.doublexplayer.fragment.VideoFragment;
import by.gravity.doublexplayer.manager.SettingsManager;

public class MainActivity extends FragmentActivity {

	private static final int LEFT_OPEN_REQUEST_CODE = 100;

	private static final int LEFT_CAMERA_REQUEST_CODE = 101;

	private static final int RIGHT_OPEN_REQUEST_CODE = 200;

	private static final int RIGHT_CAMERA_REQUEST_CODE = 201;

	private ImageView leftOpenButton;

	private ImageView leftCameraButton;

	private ImageView rightOpenButton;

	private ImageView rightCameraButton;

	private ImageView settingsButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_main);

		leftOpenButton = (ImageView) findViewById(R.id.action_bar_left)
				.findViewById(R.id.open);

		leftOpenButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				openFileAction(LEFT_OPEN_REQUEST_CODE,
						SettingsManager.getLeftPath());
			}
		});

		leftCameraButton = (ImageView) findViewById(R.id.action_bar_left)
				.findViewById(R.id.camera);
		leftCameraButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				openCameraAction(LEFT_CAMERA_REQUEST_CODE,
						SettingsManager.getLeftPath());
			}
		});

		rightOpenButton = (ImageView) findViewById(R.id.action_bar_right)
				.findViewById(R.id.open);
		rightOpenButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				openFileAction(RIGHT_OPEN_REQUEST_CODE,
						SettingsManager.getRightPath());
			}
		});

		rightCameraButton = (ImageView) findViewById(R.id.action_bar_right)
				.findViewById(R.id.camera);
		rightCameraButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				openCameraAction(RIGHT_CAMERA_REQUEST_CODE,
						SettingsManager.getRightPath());
			}
		});

		settingsButton = (ImageView) findViewById(R.id.settings);
		settingsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(MainActivity.this,
						SettingsActivity.class);
				startActivity(intent);
			}
		});

		initFragment();

	}

	public enum Video {
		LEFT, RIGHT;

	}

	public void onFullScreenClick(String tag) {

		showFullScreen(Video.valueOf(tag));

	}

	private void showFullScreen(Video video) {

		RelativeLayout activeLayout = null;
		RelativeLayout unActiveLayout = null;
		if (video == Video.LEFT) {
			activeLayout = (RelativeLayout) findViewById(R.id.leftVideoLayout);
			unActiveLayout = (RelativeLayout) findViewById(R.id.rightVideoLayout);

		} else {
			activeLayout = (RelativeLayout) findViewById(R.id.rightVideoLayout);
			unActiveLayout = (RelativeLayout) findViewById(R.id.leftVideoLayout);

		}
		if (activeLayout.getLayoutParams().width == android.widget.RelativeLayout.LayoutParams.MATCH_PARENT) {
			activeLayout
					.setLayoutParams(new LinearLayout.LayoutParams(
							0,
							android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
							0.5f));
			unActiveLayout
					.setLayoutParams(new LinearLayout.LayoutParams(
							0,
							android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
							0.5f));
		} else {
			unActiveLayout.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
			activeLayout.setLayoutParams(new LinearLayout.LayoutParams(
					android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
					android.widget.LinearLayout.LayoutParams.MATCH_PARENT));

		}
	}

	private void initFragment() {

		Fragment leftVideo = VideoFragment.newInstance();
		Fragment rightVideo = VideoFragment.newInstance();

		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.add(R.id.leftVideoLayout, leftVideo, Video.LEFT.name());
		transaction.add(R.id.rightVideoLayout, rightVideo, Video.RIGHT.name());
		transaction.commit();
	}

	private void openFileAction(int requestCode, String defaultPath) {

		Intent intent = new Intent("org.openintents.action.PICK_FILE");
		if (!StringUtil.isEmpty(defaultPath)) {
			intent.setData(Uri.parse(defaultPath));
		}
		startActivityForResult(intent, requestCode);
	}

	private void openCameraAction(int requestCode, String videoPath) {

		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		Uri uri = Uri.parse(videoPath + "/" + generateVideoName());
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		startActivityForResult(intent, requestCode);
	}

	private String generateVideoName() {

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy_MM_dd_HH_mm_ss");
		return dateFormat.format(System.currentTimeMillis()) + ".mp4";

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) {
			return;
		}
		if (requestCode == LEFT_OPEN_REQUEST_CODE) {
			setVideoFragmentUri(Video.LEFT.name(), data.getDataString());
		} else if (requestCode == RIGHT_OPEN_REQUEST_CODE) {
			setVideoFragmentUri(Video.RIGHT.name(), data.getDataString());
		} else if (requestCode == LEFT_CAMERA_REQUEST_CODE) {
			setVideoFragmentUri(Video.LEFT.name(),
					FileUtil.getFilePathFromContentUri(data.getData()));
		} else if (requestCode == RIGHT_CAMERA_REQUEST_CODE) {
			setVideoFragmentUri(Video.RIGHT.name(),
					FileUtil.getFilePathFromContentUri(data.getData()));
		}

	}

	private void setVideoFragmentUri(String tag, String mediaUri) {
		VideoFragment videoFragment = (VideoFragment) getSupportFragmentManager()
				.findFragmentByTag(tag);
		if (videoFragment != null) {
			videoFragment.init(mediaUri);
		}

	}

}
