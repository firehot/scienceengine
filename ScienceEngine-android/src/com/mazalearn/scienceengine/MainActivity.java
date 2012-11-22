package com.mazalearn.scienceengine;


import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;

public class MainActivity extends AndroidApplication {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);      
    // Android always in production mode
    ScienceEngine.DEV_MODE = DevMode.PRODUCTION;

    AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
    cfg.useGL20 = true;
    cfg.useAccelerometer = false;
    cfg.useCompass = false;
    Uri data = getIntent().getData();

    ScienceEngine scienceEngine = null;
    if (data != null) {
      scienceEngine = new ScienceEngine(data.toString());
    } else {
      scienceEngine = new ScienceEngine("");
    }
    PlatformAdapterImpl platformAdapter = new PlatformAdapterImpl(this);
    scienceEngine.setPlatformAdapter(platformAdapter);
    initialize(scienceEngine, cfg);
  }
   
  @Override
  public void exit() {
    super.exit();
    super.onDestroy();
    this.finish();
  }
}