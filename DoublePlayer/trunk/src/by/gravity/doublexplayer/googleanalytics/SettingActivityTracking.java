package by.gravity.doublexplayer.googleanalytics;

public class SettingActivityTracking {

	private static final String CATEGORY = "SettingActivity";

	private static final String NOT_CONTENT_PATH = "NotContentPath";

	private static final String CONTENT_POSITION = "ContentPosition";

	private static final String ABOUT_DEVELOPER = "AboutDeveloper";

	public static void trackSetNotContentPath(String path) {
		AnalitycsManager.getInstance().trackEvent(CATEGORY, NOT_CONTENT_PATH, path);
	}

	public static void trackContentPosition(String position) {
		AnalitycsManager.getInstance().trackEvent(CATEGORY, CONTENT_POSITION, position);
	}

	public static void trackAboutDeveloper() {
		AnalitycsManager.getInstance().trackEvent(CATEGORY, ABOUT_DEVELOPER, ABOUT_DEVELOPER);
	}
}
