package by.gravity.doubleplayer.manager;

import by.gravity.common.preference.PreferenceHelper;
import by.gravity.doubleplayer.R;

public class SettingsManager extends PreferenceHelper {

	public static String getLeftPath() {
		return getString(R.string.left_path, R.string.path_not_setted);
	}

	public static void setLeftPath(String value) {
		putString(R.string.left_path, value);
	}

	public static String getRightPath() {
		return getString(R.string.right_path,R.string.path_not_setted);
	}

	public static void setRightPath(String value) {
		putString(R.string.right_path, value);
	}

}
