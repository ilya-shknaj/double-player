package by.gravity.doublexplayer.manager;

import java.io.File;

import by.gravity.common.preference.PreferenceHelper;
import by.gravity.doublexplayer.R;

public class SettingsManager extends PreferenceHelper {

	public static String getLeftPath() {
		return getString(R.string.left_path);
	}

	public static String getLeftPathWithDefault() {
		return getString(R.string.left_path, R.string.path_not_setted);
	}

	public static void setLeftPath(String value) {
		putString(R.string.left_path, value);
	}

	public static String getRightPath() {
		return getString(R.string.right_path);
	}

	public static String getRightPathWithDefault() {
		return getString(R.string.right_path, R.string.path_not_setted);
	}

	public static void setRightPath(String value) {
		putString(R.string.right_path, value);
	}

	public static String getInfoPath() {
		String path = getString(R.string.info_path, R.string.path_not_setted);
		if (path.contains("file")) {
			return path.substring("file".length() + 2);
		}
		return path;
	}

	public static void setInfoPath(String value) {
		putString(R.string.info_path, value);
	}

}
