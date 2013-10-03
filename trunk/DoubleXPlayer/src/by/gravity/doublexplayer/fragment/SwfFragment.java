package by.gravity.doublexplayer.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import by.gravity.doubleplayer.core.IPlayer;
import by.gravity.doublexplayer.R;
import by.gravity.doublexplayer.activity.MainActivity;
import by.gravity.doublexplayer.model.Rate;

public class SwfFragment extends Fragment implements IPlayer {

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
		initButtons();
		initFlash(getMediaUriString());
	}

	private void initButtons() {
		mPlayButton = (Button) getView().findViewById(R.id.playButton);
		mPlayButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				if (isPlayed) {
					pause();
				} else {
					playPause();
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

		Button zoomInButton = (Button) getView().findViewById(R.id.zoomIn);
		zoomInButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				zoomIn();
			}
		});

		Button zoomOutButton = (Button) getView().findViewById(R.id.zoomOut);
		zoomOutButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				zoomOut();
			}
		});

		setPlayPauseButtonUI();
	}

	private void initWebView() {
		mWebView = (WebView) getView().findViewById(R.id.webView);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setAllowFileAccess(true);
		mWebView.setScrollBarStyle(0);
		mWebView.getSettings().setPluginsEnabled(true);
		mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
		mWebView.setWebChromeClient(new WebChromeClient());
		// webView.addJavascriptInterface(new CallJava(), "CallJava");
	}

	private void initFlash(String filePath) {
		StringBuffer localStringBuffer = new StringBuffer();
		localStringBuffer
				.append("<style type='text/css'>html,body,object{ margin: 0px; padding: 0px; border: 0px; width: 100%; height: 100%; overflow: hidden; background-color: #000000; color: #FF0000;} </style>");
		localStringBuffer
				.append("<object id='flashmovie' type='application/x-shockwave-flash' align='center' width='100%' height='100%' >");
		localStringBuffer.append("<param name='movie' value='" + filePath
				+ "'/>");
		localStringBuffer.append("<param name='quality' value='medium'/>");
		localStringBuffer
				.append("<param name='allowFullScreen' value='false'/>");
		localStringBuffer.append("<param name='wmode' value='direct'/>");
		localStringBuffer.append("<param name='scale' value='showall'/>");
		localStringBuffer
				.append("<param name='allowScriptAccess' value='always'/>");
		localStringBuffer.append("<center>");
		localStringBuffer
				.append("<div style='border:2px solid #666; background:#EEEEE0; position: relative; top: 100px; '>");
		localStringBuffer
				.append("<h3 style='color:red; text-align:center'>Please Install Adobe Flash Plugin and Restart !</h3>");
		localStringBuffer
				.append("<h5 style='color:#80BFFF; text-align:center'>(Some Android Devices(CPU Lower ARMv7) Can't Support Flash !)</h5>");
		localStringBuffer
				.append("<a href='https://market.android.com/details?id=com.adobe.flashplayer'>DOWNLOAD FROM MARKET</a>");
		localStringBuffer.append("</div>");
		localStringBuffer.append("</center>");
		localStringBuffer.append("</object>");
		localStringBuffer.append("<script type='text/javascript'>");
		localStringBuffer
				.append("function GetFlashInfo(){var total_frame = flashmovie.TotalFrames();var currt_frame = flashmovie.CurrentFrame();CallJava.SetFlashInfo(total_frame,currt_frame);}");
		localStringBuffer.append("function Play(){flashmovie.Play();}");
		localStringBuffer.append("function Pause(){flashmovie.StopPlay();}");
		localStringBuffer.append("function ZoomIn(){flashmovie.Zoom(90);}");
		localStringBuffer.append("function ZoomOut(){flashmovie.Zoom(110);}");
		localStringBuffer
				.append("function GotoFrame(goframe){flashmovie.GotoFrame(goframe);}");
		localStringBuffer.append("</script>");
		mWebView.loadDataWithBaseURL(null, localStringBuffer.toString(),
				"text/html", "utf-8", null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.f_video_flash, null);
	}

	private void onFullScreenClick() {

		((MainActivity) getActivity()).onFullScreenClick(getTag());

	}

	public void playPause() {
		setPlayPauseButtonUI();
		mWebView.loadUrl("javascript:Play()");
	}

	@Override
	public void playPause(boolean isPlayed) {
		this.isPlayed = isPlayed;
		if(isPlayed){
			pause();
		}else{
			pause();
		}
		setPlayPauseButtonUI();
		mWebView.loadUrl("javascript:Play()");
	}

	public void pause() {
		isPlayed = false;
		setPlayPauseButtonUI();
		mWebView.loadUrl("javascript:Pause()");
	}

	private void zoomIn() {
		mWebView.loadUrl("javascript:ZoomIn()");
	}

	private void zoomOut() {
		mWebView.loadUrl("javascript:ZoomOut()");
	}

	private void setPlayPauseButtonUI() {
		if (isPlayed) {
			mPlayButton.setBackgroundResource(R.drawable.btn_pause);
		} else {
			mPlayButton.setBackgroundResource(R.drawable.btn_play);
		}
	}

	@Override
	public String getMediaUriString() {
		return getArguments().getString(ARG_MEDIA_URI);
	}

	@Override
	public void nextFrame() {
		// TODO Auto-generated method stub

	}

	@Override
	public void prevFrame() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRate(Rate rate) {
		// TODO Auto-generated method stub

	}

}
