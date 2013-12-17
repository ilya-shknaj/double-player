package by.gravity.doublexplayer.googleanalytics;

public class VideoFragmentTracking {

	private static final String CATEGORY = "VideoFragment";

	private static final String CONTROL = "Control";

	private static final String PLAY_PAUSE = "Play/Pause";

	private static final String NEXT_FRAME = "NextFrame";

	private static final String PREV_FRAME = "PrevFrame";

	private static final String SET_RATE = "SetRate";

	private static final String FULL_SCREEN = "FullScreen";

	private static final String LEFT_FRAGMENT = "LeftFragment";

	private static final String RIGHT_FRAGMETN = "RightFragment";

	private static final String REPEAT_MODE = "RepeatMode";

	private static final String ZOOM_IN = "ZoomIn";

	private static final String ZOOM_OUT = "ZoomOut";

	private static final String NEXT_FILE = "NextFile";

	private static final String PREV_FILE = "PrevFile";

	public static void trackPlayPause() {
		trackEvent(PLAY_PAUSE);
	}

	public static void trackNextFrame() {
		trackEvent(NEXT_FRAME);
	}

	public static void trackPrevFrame() {
		trackEvent(PREV_FRAME);
	}

	public static void trackSetRate(double value) {
		trackEvent(SET_RATE + " " + String.valueOf(value));
	}

	public static void trackFullScreen() {
		trackEvent(FULL_SCREEN);
	}

	public static void trackLeftFragment() {
		trackEvent(LEFT_FRAGMENT);
	}

	public static void trackRightFragment() {
		trackEvent(RIGHT_FRAGMETN);
	}

	public static void trackRepeatMode() {
		trackEvent(REPEAT_MODE);
	}

	public static void trackZoomIn() {
		trackEvent(ZOOM_IN);
	}

	public static void trackZoomOut() {
		trackEvent(ZOOM_OUT);
	}

	public static void trackNextFile() {
		trackEvent(NEXT_FILE);
	}

	public static void trackPrevFile() {
		trackEvent(PREV_FILE);
	}

	private static void trackEvent(String label) {
		AnalitycsManager.getInstance().trackEvent(CATEGORY, CONTROL, label);
	}
}
