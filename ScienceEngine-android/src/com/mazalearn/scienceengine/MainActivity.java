package com.mazalearn.scienceengine;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.Secure;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter.Platform;
import com.mazalearn.scienceengine.billing.IabHelper;
import com.mazalearn.scienceengine.billing.IabResult;
import com.mazalearn.scienceengine.billing.Security;

public class MainActivity extends AndroidApplication {

  private IabHelper iabHelper;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);      
    // Android always in production mode
    ScienceEngine.DEV_MODE = DevMode.PRODUCTION;
    // InApp Billing helper
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
    PlatformAdapterImpl platformAdapter = new PlatformAdapterImpl(this, platform, iabHelper);
    
    ScienceEngine.setPlatformAdapter(platformAdapter);
    initialize(scienceEngine, cfg);
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
    iabHelper = null;
    super.onDestroy();
  }
}