package by.gravity.doublexplayer.manager;

import android.os.Environment;
import by.gravity.common.preference.PreferenceHelper;
import by.gravity.common.utils.ContextHolder;
import by.gravity.doublexplayer.R;

public class SettingsManager extends PreferenceHelper {

	private static String DEFAULT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

	public static String getContentPosition() {
		return getString(R.string.content, R.string.left_position);
	}

	public static void setPosition(String position) {
		putString(R.string.content, position);
	}

	public static String getContentPath() {
		if (ContextHolder.getContext().getExternalFilesDir(null) != null) {
			return ContextHolder.getContext().getExternalFilesDir(null).getAbsolutePath();
		} else {
			return "";
		}

	}

	public static String getNotContentPath() {
		return getString(R.string.not_content_path, DEFAULT_PATH);
	}

	public static String getNotContentPathWithDefault() {
		return getString(R.string.not_content_path, R.string.path_not_setted);
	}

	public static void setNotContentPath(String value) {
		putString(R.string.not_content_path, value);
	}

	public static String getLeftPath() {
		if (getContentPosition().equals(getString(R.string.left_position))) {
			return getContentPath();
		} else {
			return getNotContentPath();
		}
	}

	public static String getRightPath() {
		if (getContentPosition().equals(getString(R.string.right_position))) {
			return getContentPath();
		} else {
			return getNotContentPath();
		}
	}

}
