package com.mazalearn.scienceengine;


import java.util.Locale;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mazalearn.scienceengine.app.services.InstallProfile;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter.Platform;
import com.mazalearn.scienceengine.billing.IabHelper;
import com.mazalearn.scienceengine.billing.IabHelper.QueryInventoryFinishedListener;
import com.mazalearn.scienceengine.billing.IabResult;
import com.mazalearn.scienceengine.billing.Inventory;
import com.mazalearn.scienceengine.billing.Security;

public class MainActivity extends AndroidApplication {

  static final int TTS_CHECK = 2000;
  private IabHelper iabHelper;
  private AndroidPlatformAdapter platformAdapter;
  private TextToSpeech mTts;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);      
    // Android always in production mode
    ScienceEngine.DEV_MODE.setDebug(false);
    ScienceEngine.DEV_MODE.setDummyBilling(false);
    
    // Science engine config
    AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
    cfg.useGL20 = true;
    cfg.useAccelerometer = false;
    cfg.useCompass = false;
    Uri data = getIntent().getData();

    ScienceEngine scienceEngine = 
        new ScienceEngine(data != null ? data.toString() : "", Device.Android);
    
    // InApp Billing helper
    if (!ScienceEngine.DEV_MODE.isDummyBilling()) {
      provisionBilling();
    }
    
    Platform platform = android.os.Build.FINGERPRINT.contains("generic") 
        ? Platform.AndroidEmulator : Platform.Android;
    platformAdapter = new AndroidPlatformAdapter(this, platform, iabHelper);
    
    ScienceEngine.setPlatformAdapter(platformAdapter);
    initialize(scienceEngine, cfg);
  }

  public void provisionBilling() {
    iabHelper = new IabHelper(this, Security.getPublicKey());
    // enable debug logging (for a production application, set this to false).
    if (ScienceEngine.DEV_MODE.isDebug()) {
      iabHelper.enableDebugLogging(true);
    }
    
    iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
      public void onIabSetupFinished(IabResult result) {
        if (!result.isSuccess()) {
          // Oh noes, there was a problem.
          log(ScienceEngine.LOG, "Problem setting up In-app Billing: " + result);
          return;
        }            
        log(ScienceEngine.LOG, "In-app billing Setup successful.");
        log(ScienceEngine.LOG, "Querying inventory for owned items.");
        syncPurchaseItems();
      }
    });
  }
   
  String findAndroidId() {
    return Settings.Secure.getString(getContentResolver(), Secure.ANDROID_ID);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    log(ScienceEngine.LOG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
  
    if (requestCode == TTS_CHECK) {
      mTts = null;
      provisionTtsEngine(resultCode);
      return;
    }
    
    if (!ScienceEngine.DEV_MODE.isDummyBilling() && 
        iabHelper.handleActivityResult(requestCode, resultCode, data)) {
      return;  
    }
    super.onActivityResult(requestCode, resultCode, data);
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
            log(ScienceEngine.LOG, "TTS initialized");
          } else {
            log(ScienceEngine.LOG, "Locale not available for TTS");
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
    if (iabHelper != null) {
      try {
        iabHelper.dispose();
      } catch (IllegalArgumentException ignored) {} // Illegal if provisioning was unsuccessful
      iabHelper = null;
    }
    if (mTts != null) {
      mTts.stop();
      mTts.shutdown();
      mTts = null;
    }
    super.onDestroy();
  }

  public void syncPurchaseItems() {
    iabHelper.queryInventoryAsync(false, new QueryInventoryFinishedListener() {
      @Override
      public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
        if (result.isFailure()) {
          log(ScienceEngine.LOG, "Failed to query inventory: " + result);
          return;
        }
        InstallProfile installProfile = ScienceEngine.getPreferencesManager().getInstallProfile();
        for (String productId: inventory.getAllOwnedSkus()) {
          Topic topic = Topic.fromProductId(productId);
          if (topic == null) {
            log(ScienceEngine.LOG, "Unknown product: " + productId);
            continue;
          }
          if (!installProfile.isAvailableTopic(topic)) {
            installProfile.addAsAvailableTopic(topic);
          }
        }
      }
    });
  }
}