package by.gravity.doublexplayer.activity;

import android.support.v4.app.FragmentActivity;

import com.bugsense.trace.BugSenseHandler;
import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;

public class TrackingActivity extends FragmentActivity {
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
		BugSenseHandler.initAndStartSession(this, "b646ffe2");
		FlurryAgent.onStartSession(this, "ZJM6Q6B3KTP5RS38SBPQ");
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
		FlurryAgent.onEndSession(this);
	}
}
