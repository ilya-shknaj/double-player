package by.gravity.doublexplayer.googleanalytics;

public class MainActivityTracking {

	private static final String CATEGORY = "MainActivity";

	private static final String START_MAIN_ACTIVITY = "StartMainActivity";

	private static final String COMMON_CONTROL = "CommonControl";

	private static final String COMMON_PLAY_PAUSE = "Play/Pause";

	private static final String COMMON_NEXT_FRAME = "NextFrame";

	private static final String COMMON_PREV_FRAME = "PrevFrame";

	private static final String COMMON_SET_RATE = "SetRate";

	private static final String TOP_BAR_LEFT = "TopBarLeft";

	private static final String TOP_BAR_RIGHT = "TopBarRight";

	private static final String OPEN_FILE = "OpenFile";

	private static final String RECORD_VIDEO = "RecodrVideo";

	private static final String FILE_INFO = "FileInfo";

	private static final String TOP_BAR = "TopBar";

	private static final String OPEN_SETTINGS = "OpenSettings";

	public static void trackStartMainActivity(String packageString) {
		trackEvent(START_MAIN_ACTIVITY + " " + packageString);
	}

	public static void trackPlayPause() {
		trackEvent(COMMON_PLAY_PAUSE);
	}

	public static void trackPrevFrame() {
		trackEvent(COMMON_PREV_FRAME);
	}

	public static void trackNextFrame() {
		trackEvent(COMMON_NEXT_FRAME);
	}

	public static void trackSetRate(double value) {
		trackEvent(COMMON_SET_RATE + " " + String.valueOf(value));
	}

	public static void trackOpenFile(String expansion) {
		trackEvent(OPEN_FILE + " " + expansion);
	}

	public static void trackLeftRecordVideo() {
		trackEvent(TOP_BAR_LEFT, RECORD_VIDEO);
	}

	public static void trackLeftFileInfo() {
		trackEvent(TOP_BAR_LEFT, FILE_INFO);
	}

	public static void trackRightRecordVideo() {
		trackEvent(TOP_BAR_RIGHT, RECORD_VIDEO);
	}

	public static void trackRightFileInfo() {
		trackEvent(TOP_BAR_RIGHT, FILE_INFO);
	}

	public static void trackOpenSettings() {
		trackEvent(TOP_BAR, OPEN_SETTINGS);
	}

	private static void trackEvent(String label) {
		AnalitycsManager.getInstance().trackEvent(CATEGORY, COMMON_CONTROL, label);
	}

	private static void trackEvent(String action, String label) {
		AnalitycsManager.getInstance().trackEvent(CATEGORY, action, label);
	}

}
