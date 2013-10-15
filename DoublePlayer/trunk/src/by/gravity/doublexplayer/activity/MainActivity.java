package by.gravity.doublexplayer.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import by.gravity.common.utils.FileUtil;
import by.gravity.common.utils.StringUtil;
import by.gravity.doubleplayer.core.IPlayer;
import by.gravity.doublexplayer.R;
import by.gravity.doublexplayer.fragment.SwfFragment;
import by.gravity.doublexplayer.fragment.VideoFragment;
import by.gravity.doublexplayer.manager.SettingsManager;
import by.gravity.doublexplayer.model.Rate;
import by.gravity.doublexplayer.util.PlayerUtil;

import com.ipaulpro.afilechooser.FileListFragment;

public class MainActivity extends FragmentActivity implements FileListFragment.OnFileSelectedListener {

	private static final int LEFT_CAMERA_REQUEST_CODE = 101;

	private static final int RIGHT_CAMERA_REQUEST_CODE = 201;

	private static final int SETTINGS_UPDATE_REQUEST_CODE = 300;

	private static final String DOUBLE_PLAYER_PACKAGE = "by.gravity.doublexplayer";

	private static final String FLASH_PLAYER = "com.adobe.flashplayer";

	private static final String DOUBLE_PLAYER_APPLICATION_NAME = "CoreDoublePlayer";

	private static final String FLASH_PLAYER_APPLICATION_NAME = "Adobe Flash Player";

	private Button leftOpenButton;

	private Button leftCameraButton;

	private Button leftInfoButton;

	private Button rightOpenButton;

	private Button rightCameraButton;

	private Button rightInfoButton;

	private Button settingsButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(getContentViewResource());
		if (appplicationIsInstalled(DOUBLE_PLAYER_PACKAGE)) {
			initTopActionBar();
			initCommonActionBar();
			initFileManagerFragments();
			// initFragment();
		} else {
			showNotInstalledDialog(DOUBLE_PLAYER_APPLICATION_NAME);
		}

	}

	private boolean appplicationIsInstalled(String packageName) {
		PackageManager pm = getPackageManager();
		boolean appInstalled = false;
		try {
			pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			appInstalled = true;
		} catch (PackageManager.NameNotFoundException e) {
			appInstalled = false;
		}
		return appInstalled;
	}

	private void showNotInstalledDialog(String appName) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle(getString(R.string.not_installed_app));
		builder.setMessage(String.format(getString(R.string.app_not_installed), appName));
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		builder.show();
	}

	protected int getContentViewResource() {
		return R.layout.a_main;
	}

	public enum Position {
		LEFT, RIGHT;

	}

	public void onFullScreenClick(String tag) {

		showFullScreen(Position.valueOf(tag));

	}

	private void showFullScreen(Position video) {

		RelativeLayout activeLayout = null;
		RelativeLayout unActiveLayout = null;
		if (video == Position.LEFT) {
			activeLayout = (RelativeLayout) findViewById(R.id.leftVideoLayout);
			unActiveLayout = (RelativeLayout) findViewById(R.id.rightVideoLayout);

		} else {
			activeLayout = (RelativeLayout) findViewById(R.id.rightVideoLayout);
			unActiveLayout = (RelativeLayout) findViewById(R.id.leftVideoLayout);

		}
		if (activeLayout.getLayoutParams().width == android.widget.RelativeLayout.LayoutParams.MATCH_PARENT) {
			activeLayout.setLayoutParams(new LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 0.5f));
			unActiveLayout.setLayoutParams(new LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 0.5f));
			showCommonActionBar();
		} else {
			unActiveLayout.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
			activeLayout.setLayoutParams(new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
					android.widget.LinearLayout.LayoutParams.MATCH_PARENT));
			hideCommonActionBar();

		}
	}

	private void showCommonActionBar() {
		View commonActionBar = findViewById(R.id.commonActionBar);
		if (commonActionBar != null) {
			commonActionBar.setVisibility(View.VISIBLE);
		}
	}

	private void hideCommonActionBar() {
		View commonActionBar = findViewById(R.id.commonActionBar);
		if (commonActionBar != null) {
			commonActionBar.setVisibility(View.GONE);
		}
	}

	private void initFileManagerFragments() {
		initFileMangerFragment(Position.LEFT);
		initFileMangerFragment(Position.RIGHT);

	}

	private void initFileMangerFragment(Position position) {
		FileListFragment fileFragment = null;
		int layoutID = 0;
		if (position == Position.LEFT) {
			fileFragment = FileListFragment.newInstance(SettingsManager.getLeftPath(), true, isContentPosition(position));
			layoutID = R.id.leftVideoLayout;

		} else if (position == Position.RIGHT) {
			fileFragment = FileListFragment.newInstance(SettingsManager.getRightPath(), true, isContentPosition(position));
			layoutID = R.id.rightVideoLayout;
		}
		if (fileFragment != null && layoutID != 0) {
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.replace(layoutID, fileFragment, position.name());
			fileFragment.setOnFileSelectedListener(this);
			transaction.commit();
		}
	}

	private boolean isContentPosition(Position position) {
		if (position == Position.LEFT && SettingsManager.getLeftPath().equals(SettingsManager.getContentPath())) {
			return true;
		} else if (position == Position.RIGHT && SettingsManager.getRightPath().equals(SettingsManager.getContentPath())) {
			return true;
		}

		return false;
	}

	private void initTopActionBar() {
		leftOpenButton = (Button) findViewById(R.id.action_bar_left).findViewById(R.id.btn_open);

		leftOpenButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				initFileMangerFragment(Position.LEFT);
			}
		});

		leftCameraButton = (Button) findViewById(R.id.action_bar_left).findViewById(R.id.btn_camera);
		leftCameraButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				openCameraAction(LEFT_CAMERA_REQUEST_CODE, SettingsManager.getRightPath());
			}
		});

		leftInfoButton = (Button) findViewById(R.id.action_bar_left).findViewById(R.id.btn_info);
		leftInfoButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				showInfoDialog(Position.LEFT);
			}
		});

		rightOpenButton = (Button) findViewById(R.id.action_bar_right).findViewById(R.id.btn_open);
		rightOpenButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				initFileMangerFragment(Position.RIGHT);
			}
		});

		rightCameraButton = (Button) findViewById(R.id.action_bar_right).findViewById(R.id.btn_camera);
		rightCameraButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				openCameraAction(RIGHT_CAMERA_REQUEST_CODE, SettingsManager.getRightPath());
			}
		});

		rightInfoButton = (Button) findViewById(R.id.action_bar_right).findViewById(R.id.btn_info);
		rightInfoButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				showInfoDialog(Position.RIGHT);
			}
		});

		settingsButton = (Button) findViewById(R.id.settings);
		settingsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
				startActivityForResult(intent, SETTINGS_UPDATE_REQUEST_CODE);
			}
		});

	}

	private Rate rate = Rate.X1;

	private void initCommonActionBar() {
		View commonActionBar = findViewById(R.id.commonActionBar);

		View playButton = commonActionBar.findViewById(R.id.playButton);
		playButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				doAction(Action.PLAY_PAUSE);
			}
		});

		View prevFrame = commonActionBar.findViewById(R.id.prevFrameButton);
		prevFrame.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				doAction(Action.PREV_FRAME);

			}
		});

		View nextFrame = commonActionBar.findViewById(R.id.nextFrameButton);
		nextFrame.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				doAction(Action.NEXT_FRAME);
			}
		});

		View rateButton = commonActionBar.findViewById(R.id.rateButton);
		rateButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				rate = rate.getNext(rate);
				doAction(Action.SET_RATE);
			}
		});

	}

	private boolean isPlayed = false;

	private void doAction(Action action) {
		List<IPlayer> fragments = getVideoFragments();
		if (action == Action.PLAY_PAUSE) {
			isPlayed = !isPlayed;
		}
		for (IPlayer player : fragments) {
			switch (action) {
			case PLAY_PAUSE:
				player.playPause(isPlayed);
				updatePlayPauseUI(isPlayed);
				break;

			case NEXT_FRAME:
				player.nextFrame();
				break;

			case PREV_FRAME:
				player.prevFrame();
				break;

			case SET_RATE:
				player.setRate(rate);
				setRateUI(rate);

			default:
				break;
			}
		}
	}

	protected void setRateUI(Rate rate) {
		View commonActionBar = findViewById(R.id.commonActionBar);
		TextView rateButton = (TextView) commonActionBar.findViewById(R.id.rateButton);
		rateButton.setText(rate.getName());
	}

	protected void updatePlayPauseUI(boolean isPlaying) {
		View commonActionBar = findViewById(R.id.commonActionBar);
		View playButton = commonActionBar.findViewById(R.id.playButton);
		if (playButton == null) {
			return;
		}

		if (isPlaying) {
			playButton.setBackgroundResource(R.drawable.btn_pause);
		} else {
			playButton.setBackgroundResource(R.drawable.btn_play);
		}

	}

	private enum Action {
		PLAY_PAUSE, PREV_FRAME, NEXT_FRAME, SET_RATE

	}

	private List<IPlayer> getVideoFragments() {
		List<IPlayer> result = new ArrayList<IPlayer>();

		Fragment leftFragment = getSupportFragmentManager().findFragmentByTag(Position.LEFT.name());
		Fragment rightFragment = getSupportFragmentManager().findFragmentByTag(Position.RIGHT.name());

		if (leftFragment != null) {
			result.add((IPlayer) leftFragment);
		}

		if (rightFragment != null) {
			result.add((IPlayer) rightFragment);
		}

		return result;

	}

	private void initFragment() {

		Fragment leftVideo = VideoFragment.newInstance(null);
		Fragment rightVideo = VideoFragment.newInstance(null);
		Fragment swfFragment = SwfFragment.newInstance(null);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.leftVideoLayout, leftVideo, Position.LEFT.name());
		transaction.replace(R.id.rightVideoLayout, rightVideo, Position.RIGHT.name());
		transaction.commit();
	}

	private void openCameraAction(int requestCode, String videoPath) {

		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		if (StringUtil.isEmpty(videoPath)) {
			videoPath = FileUtil.getDefaultMediaPath();
		}
		Uri uri = Uri.fromFile(new File(videoPath + File.separator + generateVideoName()));
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
		if (requestCode == LEFT_CAMERA_REQUEST_CODE) {
			setVideoFragmentUri(Position.LEFT.name(), FileUtil.getFilePathFromContentUri(data.getData()));
		} else if (requestCode == RIGHT_CAMERA_REQUEST_CODE) {
			setVideoFragmentUri(Position.RIGHT.name(), FileUtil.getFilePathFromContentUri(data.getData()));
		} else if (requestCode == SETTINGS_UPDATE_REQUEST_CODE) {
			initFileManagerFragments();
		}

	}

	private void setVideoFragmentUri(String tag, String mediaUri) {
		Fragment fragment;
		if (mediaUri.length() > 4 && mediaUri.substring(mediaUri.length() - 3).equals("swf")) {
			if (appplicationIsInstalled(FLASH_PLAYER)) {
				fragment = SwfFragment.newInstance(mediaUri);
			} else {
				showNotInstalledDialog(FLASH_PLAYER_APPLICATION_NAME);
				return;
			}
		} else {
			fragment = VideoFragment.newInstance(mediaUri);

		}

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		if (tag.intern() == Position.LEFT.name().intern()) {
			transaction.replace(R.id.leftVideoLayout, fragment, tag);
		} else {
			transaction.replace(R.id.rightVideoLayout, fragment, tag);
		}
		transaction.commit();

	}

	private void showInfoDialog(Position position) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.d_info, null);
		TextView textView = (TextView) dialogView.findViewById(R.id.text);
		String info = getMediaInfo(position);
		if (info == null) {
			info = getString(R.string.info_not_found);
		}
		textView.setText(info);
		builder.setView(dialogView);
		builder.setPositiveButton(android.R.string.ok, null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private String getMediaInfo(Position position) {
		Fragment fragment = getSupportFragmentManager().findFragmentByTag(position.name());
		if (fragment != null) {
			IPlayer video = (IPlayer) fragment;
			String mediaUri = video.getMediaUriString();
			if (mediaUri != null) {
				mediaUri = StringUtil.decodeString(mediaUri);
				String txtPath = PlayerUtil.changeFileExtensionToTxt(mediaUri);
				return FileUtil.readFileAsString(txtPath);
			}
		}

		return null;
	}

	@Override
	public void onFileSelected(String tag, String filePath) {
		if (tag.equals(Position.LEFT.name())) {
			setVideoFragmentUri(Position.LEFT.name(), filePath);
		} else if (tag.equals(Position.RIGHT.name())) {
			setVideoFragmentUri(Position.RIGHT.name(), filePath);
		}
	}

}
