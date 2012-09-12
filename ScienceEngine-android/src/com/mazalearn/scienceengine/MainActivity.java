package com.mazalearn.scienceengine;

import java.util.List;

import android.net.Uri;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mazalearn.scienceengine.ScienceEngine;
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

        if (data != null) {
          List<String> params = data.getPathSegments();
          initialize(new ScienceEngine(params), cfg);
        } else {
          initialize(new ScienceEngine(), cfg);
        }
    }
}