package by.gravity.doublexplayer.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import by.gravity.doublexplayer.R;
import by.gravity.doublexplayer.googleanalytics.SettingActivityTracking;
import by.gravity.doublexplayer.manager.SettingsManager;

import com.ipaulpro.afilechooser.FolderChooseActivity;

public class SettingsActivity extends PreferenceActivity {

	private static final int SELECT_PATH_REQUEST_CODE = 1;

	private Preference notContentPath;

	private Preference contentPosition;

	private Preference aboutDeveloper;

	private boolean valuesChanged = false;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		contentPosition = findPreference(getString(R.string.content));
		contentPosition.setSummary(SettingsManager.getContentPosition());
		contentPosition.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference paramPreference, Object paramObject) {
				SettingsManager.setPosition((String) paramObject);
				contentPosition.setSummary((CharSequence) paramObject);
				SettingActivityTracking.trackContentPosition((String) paramObject);
				updateNotContentPathTitle();
				if (!valuesChanged) {
					valuesChanged = true;
				}
				return true;
			}
		});

		notContentPath = findPreference(getString(R.string.not_content_path));
		notContentPath.setSummary(SettingsManager.getNotContentPathWithDefault());
		notContentPath.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				startSelectFolder(SELECT_PATH_REQUEST_CODE, SettingsManager.getNotContentPath());

				return false;
			}
		});

		aboutDeveloper = findPreference(getString(R.string.about_developer));
		aboutDeveloper.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference paramPreference) {
				showAboutDeveloperDialog();
				SettingActivityTracking.trackAboutDeveloper();
				return false;
			}
		});

		updateNotContentPathTitle();

	}

	private void updateNotContentPathTitle() {
		String panelName = null;

		if (SettingsManager.getContentPosition().equals(getString(R.string.left_position))) {
			panelName = String.format(getString(R.string.not_content_panel), getString(R.string.right));
		} else {
			panelName = String.format(getString(R.string.not_content_panel), getString(R.string.left));
		}
		notContentPath.setTitle(panelName);
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
		SettingsManager.setNotContentPath(uriString);
		updateSummary(notContentPath, uriString);
		SettingActivityTracking.trackSetNotContentPath(uriString);
		if (!valuesChanged) {
			valuesChanged = true;
		}
	}

	@Override
	public void onBackPressed() {
		if (valuesChanged) {
			setResult(RESULT_OK);
		}
		finish();
	}

	private void updateSummary(Preference preference, String value) {
		preference.setSummary(value);
	}

	private void showAboutDeveloperDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle(getString(R.string.about_developer));
		builder.setMessage(getString(R.string.about_developer_text));
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.show();
	}

}
