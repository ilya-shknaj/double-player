package by.gravity.doublexplayer.util;

public class PlayerUtil {

	public static String changeFileExtensionToTxt(String path) {
		int index = path.lastIndexOf(".");
		if (index != -1) {
			return path.substring(0, index + 1) + "txt";
		}

		return null;
	}
}
