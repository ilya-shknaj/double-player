package by.gravity.doublexplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import by.gravity.doublexplayer.R;
import by.gravity.doublexplayer.manager.SettingsManager;

import com.ipaulpro.afilechooser.FolderChooseActivity;

public class SettingsActivity extends PreferenceActivity {

	private static final int LEFT_PATH = 1;

	private static final int RIGHT_PATH = 2;

	private Preference leftPath;

	private Preference rightPath;

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
				startSelectFolder(LEFT_PATH, SettingsManager.getLeftPath());
				return false;
			}
		});

		rightPath = findPreference(getString(R.string.right_path));
		rightPath.setSummary(SettingsManager.getRightPathWithDefault());
		rightPath.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				startSelectFolder(RIGHT_PATH, SettingsManager.getRightPath());
				return false;
			}
		});

	}

	private void startSelectFolder(int requestCode, String defaulPath) {
		Intent intent = new Intent(this, FolderChooseActivity.class);
		intent.putExtra(FolderChooseActivity.EXTRA_START_PATH, defaulPath);
		startActivityForResult(intent, requestCode);
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
		}
	}

	private void updateSummary(Preference preference, String value) {
		preference.setSummary(value);
	}

}
