package by.gravity.doublexplayer.fragment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import by.gravity.doublexplayer.R;
import by.gravity.doublexplayer.activity.MainActivity;

public class SwfFragment extends Fragment {

	private WebView mWebView;

	private static final String DEFAULT_URI = "file:///"
			+ Environment.getExternalStorageDirectory().getAbsolutePath()
			+ "/DoublePlayer/swf/922.swf";

	private static final String ARG_MEDIA_URI = "ARG_MEDIA_URI";

	private boolean isPlayed = true;

	private Button mPlayButton;

	public static SwfFragment newInstance(String mediaUri) {
		SwfFragment fragment = new SwfFragment();

		Bundle bundle = new Bundle();
		if (mediaUri == null) {
			mediaUri = DEFAULT_URI;
		}
		bundle.putString(ARG_MEDIA_URI, mediaUri);

		fragment.setArguments(bundle);

		return fragment;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initWebView();
		initProgressButtons();
	}

	private void initProgressButtons() {
		mPlayButton = (Button) getView().findViewById(R.id.playButton);
		mPlayButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				if (isPlayed) {
					pause();
				} else {
					play();
				}

			}
		});

		Button fullScreenButton = (Button) getView().findViewById(
				R.id.fullScreenButton);
		fullScreenButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				onFullScreenClick();
			}
		});

		setPlayPauseButtonUI();
	}

	private void initWebView() {
		mWebView = (WebView) getView().findViewById(R.id.webView);

		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setAllowFileAccess(true);
		mWebView.getSettings().setPluginsEnabled(true);
		mWebView.getSettings().setSupportZoom(false);
		mWebView.getSettings().setAppCacheEnabled(false);
		mWebView.getSettings().setAllowContentAccess(true);
		mWebView.getSettings().setCacheMode(
				mWebView.getSettings().LOAD_NO_CACHE);
		mWebView.setBackgroundColor(Color.BLACK);
		mWebView.loadUrl(getArguments().getString(ARG_MEDIA_URI));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.f_video_flash, null);
	}

	private void onFullScreenClick() {

		((MainActivity) getActivity()).onFullScreenClick(getTag());

	}

	public void play() {
		isPlayed = true;
		setPlayPauseButtonUI();
		callHiddenWebViewMethod("onResume");
	}

	public void pause() {
		isPlayed = false;
		setPlayPauseButtonUI();
		callHiddenWebViewMethod("onPause");
	}

	private void setPlayPauseButtonUI() {
		if (isPlayed) {
			mPlayButton.setBackgroundResource(R.drawable.btn_pause);
		} else {
			mPlayButton.setBackgroundResource(R.drawable.btn_play);
		}
	}

	private void callHiddenWebViewMethod(String name) {
		if (mWebView != null) {
			Method method;
			try {
				method = WebView.class.getMethod(name);
				method.invoke(mWebView);
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}
