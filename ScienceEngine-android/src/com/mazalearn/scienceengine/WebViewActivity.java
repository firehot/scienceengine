package com.mazalearn.scienceengine;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends Activity
implements OnCompletionListener {
  private WebView myWebView;

  @Override
  public void onCreate(Bundle savedState) {
    super.onCreate(savedState);
    setContentView(R.layout.webview);
    myWebView = (WebView) findViewById(R.id.webview);
    WebSettings webSettings = myWebView.getSettings();
    webSettings.setJavaScriptEnabled(true);
    webSettings.setSupportZoom(false);
    webSettings.setAllowFileAccess(true);
    webSettings.setLoadsImagesAutomatically(true);

    String dataString = getIntent().getDataString();
    myWebView.setWebViewClient(new WebViewClient());
    // this is necessary for "alert()" to work
    myWebView.setWebChromeClient(new WebChromeClient());
    myWebView.loadUrl(dataString);
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
  }

  @Override
  public void onCompletion(MediaPlayer mp) {
    finish();
  }

}
