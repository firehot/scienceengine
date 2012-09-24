package com.mazalearn.scienceengine;

import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.app.utils.UrlFetcher;

public class MainActivity extends AndroidApplication implements UrlFetcher {
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
          List<String> params = data.getPathSegments();
          scienceEngine = new ScienceEngine(params);
        } else {
          scienceEngine = new ScienceEngine();
        }
        scienceEngine.setUrlFetcher(this);
        initialize(scienceEngine, cfg);
    }
    
    public void fetchURL(String url) {
      Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
      startActivity(myIntent);
    }   
}