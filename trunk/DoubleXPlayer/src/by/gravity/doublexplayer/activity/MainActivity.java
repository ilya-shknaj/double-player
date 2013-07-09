package by.gravity.doublexplayer.activity;

import java.text.SimpleDateFormat;

import android.content.Intent;
import android.media.MediaPlayer.TrackInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import by.gravity.common.utils.StringUtil;
import by.gravity.doublexplayer.R;
import by.gravity.doublexplayer.fragment.VideoFragment;
import by.gravity.doublexplayer.manager.SettingsManager;
import by.gravity.doublexplayer.model.VideoState;

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

		initFragment();

	}

	private enum Video {
		LEFT, RIGHT;
	}

	public void showFullScreen(String tag) {
		showFullScreen(Video.valueOf(tag));
	}

	private void showFullScreen(Video video) {
		LinearLayout layout = null;
		if (video == Video.LEFT) {
			layout = (LinearLayout) findViewById(R.id.leftVideoLayout);

		} else {
			layout = (LinearLayout) findViewById(R.id.rightVideoLayout);

		}
		layout.setLayoutParams(new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.FILL_PARENT,
				android.widget.LinearLayout.LayoutParams.FILL_PARENT));
	}

	private void hideFullScreen() {

	}

	private void initFragment() {

		Fragment leftVideo = VideoFragment.newInstance(null);
		Fragment rightVideo = VideoFragment.newInstance(null);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.add(R.id.leftVideoLayout, leftVideo, Video.LEFT.toString());
		transaction.add(R.id.rightVideoLayout, rightVideo, Video.RIGHT.toString());
		transaction.commit();
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

	}

}
