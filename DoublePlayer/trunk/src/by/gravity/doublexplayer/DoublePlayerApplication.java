package by.gravity.doublexplayer;

import by.gravity.common.CoreApplication;
import by.gravity.doublexplayer.googleanalytics.AnalitycsManager;

public class DoublePlayerApplication extends CoreApplication {

	@Override
	public void onCreate() {
		super.onCreate();
		AnalitycsManager.startAnalytics(getApplicationContext());
	}

}
