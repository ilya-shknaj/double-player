package by.gravity.doublexplayer.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;
import by.gravity.doublexplayer.R;
import by.gravity.doublexplayer.manager.SettingsManager;

public class SettingsActivity extends PreferenceActivity {

	private static final int LEFT_PATH = 1;

	private static final int RIGHT_PATH = 2;

	private static final int INFO_PATH = 3;

	private Preference leftPath;

	private Preference rightPath;

	private Preference infoPath;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		leftPath = findPreference(getString(R.string.left_path));
		leftPath.setSummary(SettingsManager.getLeftPathWithDefault());
		leftPath.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				startSelectFolder(LEFT_PATH);
				return false;
			}
		});

		rightPath = findPreference(getString(R.string.right_path));
		rightPath.setSummary(SettingsManager.getRightPathWithDefault());
		rightPath.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				startSelectFolder(RIGHT_PATH);
				return false;
			}
		});

		infoPath = findPreference(getString(R.string.info_path));
		infoPath.setSummary(SettingsManager.getInfoPath());
		infoPath.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference paramPreference) {
				startSelectFolder(INFO_PATH);
				return false;
			}
		});

	}

	private void startSelectFolder(int requestCode) {
		Intent intent = new Intent("org.openintents.action.PICK_DIRECTORY");
		try {
			startActivityForResult(intent, requestCode);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, "File manager not found", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data == null) {
			return;
		}
		String uriString = data.getData().toString();
		if (requestCode == LEFT_PATH) {
			SettingsManager.setLeftPath(uriString);
			updateSummary(leftPath, uriString);
		} else if (requestCode == RIGHT_PATH) {
			SettingsManager.setRightPath(uriString);
			updateSummary(rightPath, uriString);
		} else if (requestCode == INFO_PATH) {
			SettingsManager.setInfoPath(uriString);
			updateSummary(infoPath, uriString);
		}
	}

	private void updateSummary(Preference preference, String value) {
		preference.setSummary(value);
	}

}
