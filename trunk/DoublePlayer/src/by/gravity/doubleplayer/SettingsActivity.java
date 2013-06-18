package by.gravity.doubleplayer;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {

	private static final int LEFT_OPEN_PATH = 1;

	private static final int LEFT_CAMERA_PATH = 2;

	private static final int RIGHT_OPEN_PATH = 3;

	private static final int RIGHT_CAMERA_PATH = 4;

	private Preference leftOpenPath;

	private Preference leftCameraPath;

	private Preference rightOpenPath;

	private Preference rightCameraPath;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);

		leftOpenPath = findPreference(getString(R.string.left_path));
		leftOpenPath.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				startSelectFolder(LEFT_OPEN_PATH);
				return false;
			}
		});

		leftCameraPath = findPreference(getString(R.string.left_camera_path));
		leftCameraPath.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				startSelectFolder(LEFT_CAMERA_PATH);
				return false;
			}
		});

		rightOpenPath = findPreference(getString(R.string.right_path));
		rightOpenPath.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				startSelectFolder(RIGHT_OPEN_PATH);
				return false;
			}
		});

		rightCameraPath = findPreference(getString(R.string.right_camera_path));
		rightCameraPath.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				startSelectFolder(RIGHT_CAMERA_PATH);
				return false;
			}
		});
	}

	private void startSelectFolder(int requestCode) {
		Intent intent = new Intent("org.openintents.action.PICK_DIRECTORY");
		try {
			startActivityForResult(intent, requestCode);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, "File manager not found", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data == null) {
			return;
		}
		Uri uri = data.getData();
		if (requestCode == LEFT_OPEN_PATH) {
			setValue(leftOpenPath, uri);
		} else if (requestCode == LEFT_CAMERA_PATH) {
			setValue(leftCameraPath, uri);
		} else if (requestCode == RIGHT_OPEN_PATH) {
			setValue(rightOpenPath, uri);
		} else if (requestCode == RIGHT_CAMERA_PATH) {
			setValue(rightCameraPath, uri);
		}
	}

	private void setValue(Preference preference, Uri uri) {
		preference.setDefaultValue(uri.getPath().toString());
		preference.setSummary(uri.getPath().toString());
		preference.shouldCommit();
	}

}
