package by.gravity.doublexplayer.activity;

import android.support.v4.app.FragmentActivity;

import com.bugsense.trace.BugSenseHandler;
import com.google.analytics.tracking.android.EasyTracker;

public class TrackingActivity extends FragmentActivity {
	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
		BugSenseHandler.initAndStartSession(this, "b646ffe2");
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}
}
