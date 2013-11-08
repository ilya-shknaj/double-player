package by.gravity.doublexplayer.googleanalytics;

import android.content.Context;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

public class AnalitycsManager {

	private static final String UA_ACCOUNT_CODE = "UA-33534273-2";

	private static AnalitycsManager instance;

	private final Tracker gaTracker;

	private final GoogleAnalytics gaInstance;

	private AnalitycsManager(Context context) {
		gaInstance = GoogleAnalytics.getInstance(context);
		gaTracker = gaInstance.getTracker(UA_ACCOUNT_CODE);

	}

	public static void startAnalytics(Context context) {
		if (instance == null) {
			instance = new AnalitycsManager(context);
		} else {
			throw new IllegalStateException("AnalitycsManager is already started");
		}
	}

	public static AnalitycsManager getInstance() {
		if (instance == null) {
			throw new IllegalStateException("AnalitycsManager is not started yet");
		}
		return instance;
	}

	void trackEvent(String category, String action, String label) {
		gaTracker.sendEvent(category, action, label, 0l);
	}

}
