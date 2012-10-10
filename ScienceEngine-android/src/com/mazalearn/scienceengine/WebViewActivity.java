package com.mazalearn.scienceengine;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends Activity
implements OnCompletionListener {
  @Override
  public void onCreate(Bundle savedState) {
    super.onCreate(savedState);
    setContentView(R.layout.main);
    WebView myWebView = (WebView) findViewById(R.id.webview);
    myWebView.loadUrl(getIntent().getDataString());
    myWebView.setWebViewClient(new WebViewClient());
    // this is necessary for "alert()" to work
    myWebView.setWebChromeClient(new WebChromeClient());
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
  }

  @Override
  public void onCompletion(MediaPlayer mp) {
      // The video has finished, return from this activity
      finish();
  }
}
