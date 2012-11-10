package com.mazalearn.scienceengine;

import java.io.File;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.app.utils.PlatformAdapter;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.designer.LevelEditor;

public class MainActivity extends AndroidApplication implements PlatformAdapter {
    private IMessage messages;

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
      scienceEngine.setPlatformAdapter(this);
      initialize(scienceEngine, cfg);
    }
    
    
    @Override
    public void exit() {
      super.exit();
      super.onDestroy();
      this.finish();
    }
    
    @Override
    public Platform getPlatform() {
      return android.os.Build.FINGERPRINT.contains("generic") 
          ? Platform.AndroidEmulator : Platform.Android;
    }
    
    public IMessage getMsg() {
      if (messages == null) {
        messages = new Messages(android.os.Build.FINGERPRINT.contains("generic") 
            ? Platform.AndroidEmulator : Platform.Android);
      }
      return messages;
    }
    
    public void browseURL(String url) {
      Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
      startActivity(myIntent);
    }

    public void showURL(String url) {
      Intent myIntent = 
          new Intent("com.mazalearn.scienceengine.intent.action.WebViewActivity");
      myIntent.setData(Uri.parse(url));
      startActivity(myIntent);
    }

    @Override
    public boolean playVideo(File file) {
      Intent videoPlayback = new Intent(this, VideoPlayer.class);
      // videoPlayback.setData(Uri.fromFile(file));
      videoPlayback.putExtra("com.mazalearn.scienceengine.FileName", 
          file.getAbsolutePath());
      startActivity(videoPlayback);
      return true;
    }

    @Override
    public Stage createLevelEditor(IScience2DController science2DController,
        AbstractScreen screen) {
      return new LevelEditor(science2DController, screen);
    }

    @Override
    public BitmapFont getFont(int pointSize) {
      FileHandle fontFileHandle = Gdx.files.internal("skin/Roboto-Regular.ttf");
      StringBuilder characters = new StringBuilder();
      for (char c = 0; c <= 127; c++) {
        characters.append(c);
      }
      FreeTypeFontGenerator generator = 
          new FreeTypeFontGenerator(fontFileHandle);
      BitmapFont font = generator.generateFont(pointSize, characters.toString(), false);
      generator.dispose();
      return font;
    }
}