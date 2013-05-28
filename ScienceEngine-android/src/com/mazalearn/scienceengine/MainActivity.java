package com.mazalearn.scienceengine;


import java.util.Locale;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter.Platform;
import com.mazalearn.scienceengine.billing.IabHelper;
import com.mazalearn.scienceengine.billing.IabResult;
import com.mazalearn.scienceengine.billing.Security;

public class MainActivity extends AndroidApplication {

  private static final int TTS_CHECK = 2000;
  private IabHelper iabHelper;
  private AndroidPlatformAdapter platformAdapter;
  private TextToSpeech mTts;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);      
    // Android always in production mode
    ScienceEngine.DEV_MODE = DevMode.PRODUCTION;
    
    // InApp Billing helper
    provisionBilling();
    
    // Science engine config
    AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
    cfg.useGL20 = true;
    cfg.useAccelerometer = false;
    cfg.useCompass = false;
    Uri data = getIntent().getData();

    ScienceEngine scienceEngine = 
        new ScienceEngine(data != null ? data.toString() : "", Device.Android);
    
    Platform platform = android.os.Build.FINGERPRINT.contains("generic") 
        ? Platform.AndroidEmulator : Platform.Android;
    platformAdapter = new AndroidPlatformAdapter(this, platform, iabHelper);
    
    // See if TTS engine can be started
    if (platformAdapter.supportsSpeech()) {
      Intent checkIntent = new Intent();
      checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
      startActivityForResult(checkIntent, TTS_CHECK);
    }
    
    ScienceEngine.setPlatformAdapter(platformAdapter);
    initialize(scienceEngine, cfg);
  }

  public void provisionBilling() {
    iabHelper = new IabHelper(this, Security.getPublicKey());
    // enable debug logging (for a production application, set this to false).
    iabHelper.enableDebugLogging(true);
    
    iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
      public void onIabSetupFinished(IabResult result) {
        if (!result.isSuccess()) {
          // Oh noes, there was a problem.
          Gdx.app.log(ScienceEngine.LOG, "Problem setting up In-app Billing: " + result);
          return;
        }            
        Gdx.app.log(ScienceEngine.LOG, "In-app billing Setup successful.");
      }
    });
  }
   
  String findAndroidId() {
    return Settings.Secure.getString(getContentResolver(), Secure.ANDROID_ID);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    log(ScienceEngine.LOG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
  
     if (!iabHelper.handleActivityResult(requestCode, resultCode, data)) {
       super.onActivityResult(requestCode, resultCode, data);  
     }
     mTts = null;
     if (requestCode == TTS_CHECK) {
       provisionTtsEngine(resultCode);
     }
  }

  private void provisionTtsEngine(int resultCode) {
    if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
       // success, create the TTS instance
       mTts = new TextToSpeech(this, new OnInitListener() {
        @Override
        public void onInit(int arg0) {
          int available = mTts.isLanguageAvailable(Locale.US);
          if (available != TextToSpeech.LANG_MISSING_DATA && 
              available != TextToSpeech.LANG_NOT_SUPPORTED) {
            mTts.setLanguage(Locale.US);
            mTts.setSpeechRate(1.0f);
            mTts.setPitch(0.9f);
            platformAdapter.setTts(mTts);
            Gdx.app.log(ScienceEngine.LOG, "TTS initialized");
          } else {
            Gdx.app.error(ScienceEngine.LOG, "Locale not available for TTS");
            mTts.stop();
            mTts.shutdown();
            mTts = null;
          }
        }});
     } else {
       // missing data, install it
       Intent installIntent = new Intent();
       installIntent.setAction(
           TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
       startActivity(installIntent);
     }
  }
  
  @Override
  public void exit() {
    super.exit();
    onDestroy();
    this.finish();
  }
  
  @Override
  public void onDestroy() {
    if (iabHelper != null) iabHelper.dispose();
    if (mTts != null) {
      mTts.stop();
      mTts.shutdown();
      mTts = null;
    }
    iabHelper = null;
    super.onDestroy();
  }
}