package com.mazalearn.scienceengine;

import java.io.File;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.FreeTypeComplexFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.billing.util.IabHelper;
import com.mazalearn.scienceengine.billing.util.IabHelper.OnIabPurchaseFinishedListener;
import com.mazalearn.scienceengine.billing.util.IabResult;
import com.mazalearn.scienceengine.billing.util.Purchase;
import com.mazalearn.scienceengine.tutor.IDoneCallback;

public class PlatformAdapterImpl extends NonWebPlatformAdapter {
  private AndroidApplication application;
  private IabHelper iabHelper;
  
  public PlatformAdapterImpl(AndroidApplication application, Platform platform, IabHelper iabHelper) {
    super(platform);
    this.application = application;
    this.iabHelper = iabHelper;
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
  public void launchPurchaseFlow(String sku, String itemType, final IDoneCallback doneCallback, String extraData) {
    iabHelper.launchPurchaseFlow(application, sku, itemType, 1234,
        new OnIabPurchaseFinishedListener() {
          @Override
          public void onIabPurchaseFinished(IabResult result, Purchase info) {
            doneCallback.done(result.isSuccess() || 
                result.getResponse() == IabHelper.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED);
          } 
        }, extraData);
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
  
  @Override
  public boolean supportsLanguage() {
    return true;
  }
  
  public String getUserEmail() {
    for (Account a: AccountManager.get(application).getAccountsByType("com.google")) {
      if (a.name.contains("@gmail.com")) {
        return a.name;
      }
    }    
    return "";
  }
}