package com.mazalearn.scienceengine;

import java.io.File;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.FreeTypeComplexFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.app.services.IMessage;

public class PlatformAdapterImpl extends NonWebPlatformAdapter {
  private AndroidApplication application;
  
  public PlatformAdapterImpl(AndroidApplication application, Platform platform) {
    super(platform);
    this.application = application;
  }

  public IMessage getMsg() {
    if (messages == null) {
      messages = new Messages(getPlatform());
    }
    return messages;
  }
  
  @Override
  public void browseURL(String url) {
    Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    application.startActivity(myIntent);
  }

  @Override
  public void showExternalURL(String url) {
    showFileUri(url, Environment.getExternalStorageDirectory().toString());
  }

  private void showFileUri(String url, String directory) {
    Intent myIntent = 
        new Intent("com.mazalearn.scienceengine.intent.action.WebViewActivity");
    myIntent.setData( 
        Uri.parse("file://" + directory + "/" + url));
    application.startActivity(myIntent);
  }

  @Override
  public void showInternalURL(String url) {
    showFileUri(url, "android_asset");
  }

  @Override
  public boolean playVideo(File file) {
    Intent videoPlayback = new Intent(application, VideoPlayer.class);
    // videoPlayback.setData(Uri.fromFile(file));
    videoPlayback.putExtra("com.mazalearn.scienceengine.FileName", 
        file.getAbsolutePath());
    application.startActivity(videoPlayback);
    return true;
  }

  @Override
  public BitmapFont getScaledFont(int pointSize) {
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
  
  private static final String HINDI_TTF = "Lohit-Devanagari.ttf"; // "aksharhindi.ttf";
  private static final String KANNADA_TTF = "Lohit-Kannada.ttf"; // "aksharkannada.ttf";
  
  @Override
  public BitmapFont loadFont(Skin skin, String language) {
    BitmapFont font;
    String fontFileName = null;
    if (language.equals("ka")) {
      fontFileName = KANNADA_TTF; // unicode: 0C80-0CFF
    } else if (language.equals("hi")) {
      fontFileName =  HINDI_TTF; // unicode: 0900-097F
    }
    BitmapFontCache.setFallbackFont(skin.getFont("en"));
    FileHandle fontFileHandle = Gdx.files.internal("skin/" + fontFileName);
    BitmapFontCache.setComplexScriptLayout(language, fontFileName);
    FreeTypeComplexFontGenerator generator = 
        new FreeTypeComplexFontGenerator(fontFileHandle);
    font = generator.generateFont(16, false);
    generator.dispose();
    return font;
  }
}