package by.gravity.doublexplayer.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import by.gravity.common.utils.FileUtil;
import by.gravity.common.utils.StringUtil;
import by.gravity.doubleplayer.core.IPlayer;
import by.gravity.doublexplayer.R;
import by.gravity.doublexplayer.fragment.SwfFragment;
import by.gravity.doublexplayer.fragment.VideoFragment;
import by.gravity.doublexplayer.manager.SettingsManager;
import by.gravity.doublexplayer.model.Rate;
import by.gravity.doublexplayer.util.PlayerUtil;

public class MainActivity extends FragmentActivity {

	private static final int LEFT_OPEN_REQUEST_CODE = 100;

	private static final int LEFT_CAMERA_REQUEST_CODE = 101;

	private static final int RIGHT_OPEN_REQUEST_CODE = 200;

	private static final int RIGHT_CAMERA_REQUEST_CODE = 201;

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
		setContentView(R.layout.a_main);
		initTopActionBar();
		initCommonActionBar();
		initFragment();

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
			showCommonActionBar();
		} else {
			unActiveLayout.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
			activeLayout.setLayoutParams(new LinearLayout.LayoutParams(
					android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
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

	private void initTopActionBar() {
		leftOpenButton = (Button) findViewById(R.id.action_bar_left)
				.findViewById(R.id.btn_open);

		leftOpenButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				openFileAction(LEFT_OPEN_REQUEST_CODE,
						SettingsManager.getLeftPath());
			}
		});

		leftCameraButton = (Button) findViewById(R.id.action_bar_left)
				.findViewById(R.id.btn_camera);
		leftCameraButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				openCameraAction(LEFT_CAMERA_REQUEST_CODE,
						SettingsManager.getLeftPath());
			}
		});

		leftInfoButton = (Button) findViewById(R.id.action_bar_left)
				.findViewById(R.id.btn_info);
		leftInfoButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				showInfoDialog(Position.LEFT);
			}
		});

		rightOpenButton = (Button) findViewById(R.id.action_bar_right)
				.findViewById(R.id.btn_open);
		rightOpenButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				openFileAction(RIGHT_OPEN_REQUEST_CODE,
						SettingsManager.getRightPath());
			}
		});

		rightCameraButton = (Button) findViewById(R.id.action_bar_right)
				.findViewById(R.id.btn_camera);
		rightCameraButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				openCameraAction(RIGHT_CAMERA_REQUEST_CODE,
						SettingsManager.getRightPath());
			}
		});

		rightInfoButton = (Button) findViewById(R.id.action_bar_right)
				.findViewById(R.id.btn_info);
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

				Intent intent = new Intent(MainActivity.this,
						SettingsActivity.class);
				startActivity(intent);
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
		TextView rateButton = (TextView) commonActionBar
				.findViewById(R.id.rateButton);
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

		Fragment leftFragment = getSupportFragmentManager().findFragmentByTag(
				Position.LEFT.name());
		Fragment rightFragment = getSupportFragmentManager().findFragmentByTag(
				Position.RIGHT.name());

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

		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.leftVideoLayout, leftVideo,
				Position.LEFT.name());
		transaction.replace(R.id.rightVideoLayout, rightVideo,
				Position.RIGHT.name());
		transaction.commit();
	}

	private void openFileAction(int requestCode, String defaultPath) {

		Intent intent = new Intent("org.openintents.action.PICK_FILE");
		if (!StringUtil.isEmpty(defaultPath)) {
			intent.setData(Uri.parse(defaultPath));
		}
		try {
			startActivityForResult(intent, requestCode);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, "IO File Manager not installed",
					Toast.LENGTH_LONG).show();
		}
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
			setVideoFragmentUri(Position.LEFT.name(), data.getDataString());
		} else if (requestCode == RIGHT_OPEN_REQUEST_CODE) {
			setVideoFragmentUri(Position.RIGHT.name(), data.getDataString());
		} else if (requestCode == LEFT_CAMERA_REQUEST_CODE) {
			setVideoFragmentUri(Position.LEFT.name(),
					FileUtil.getFilePathFromContentUri(data.getData()));
		} else if (requestCode == RIGHT_CAMERA_REQUEST_CODE) {
			setVideoFragmentUri(Position.RIGHT.name(),
					FileUtil.getFilePathFromContentUri(data.getData()));
		}

	}

	private void setVideoFragmentUri(String tag, String mediaUri) {
		Fragment fragment;
		if (mediaUri.length() > 4
				&& mediaUri.substring(mediaUri.length() - 3).equals("swf")) {
			fragment = SwfFragment.newInstance(mediaUri);
		} else {
			fragment = VideoFragment.newInstance(mediaUri);

		}

		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
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
		builder.setPositiveButton("Ok", null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private String getMediaInfo(Position position) {
		Fragment fragment = getSupportFragmentManager().findFragmentByTag(
				position.name());
		if (fragment != null) {
			IPlayer video = (IPlayer) fragment;
			String mediaUri = video.getMediaUriString();
			if (mediaUri != null) {
				mediaUri = StringUtil.decodeString(mediaUri);
				String txtPath = PlayerUtil.changeFileExtensionToTxt(FileUtil
						.getFileNameFromPath(mediaUri));
				return FileUtil.readFileAsString(SettingsManager.getInfoPath()
						+ File.separator + txtPath);
			}
		}

		return null;
	}

}
