package com.mazalearn.scienceengine;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.billing.IBilling;
import com.mazalearn.scienceengine.billing.IabException;
import com.mazalearn.scienceengine.billing.IabHelper;
import com.mazalearn.scienceengine.billing.IabHelper.OnIabPurchaseFinishedListener;
import com.mazalearn.scienceengine.billing.IabResult;
import com.mazalearn.scienceengine.billing.Inventory;
import com.mazalearn.scienceengine.billing.Purchase;

public class PlatformAdapterImpl extends NonWebPlatformAdapter {
  private AndroidApplication application;
  private IabHelper iabHelper;
  protected static final String SKU_ELECTROMAGNETISM = "electromagnetism";
  
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
  public void launchPurchaseFlow(final Topic topic, final IBilling billing) {
    iabHelper.launchPurchaseFlow(application, topic.toProductId(), IabHelper.ITEM_TYPE_INAPP, 
        IBilling.REQUEST_CODE,
        new OnIabPurchaseFinishedListener() {
          @Override
          public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.getResponse() == IabHelper.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED) {
              // In this case, the purchase info is missing - we allow this
              billing.purchaseCallback(topic);
            }

            if (!verifyDeveloperPayload(purchase, topic)) {
              Gdx.app.error(ScienceEngine.LOG, "Error purchasing. Authenticity verification failed.");
              return;
            }
            if (result.isSuccess()) {
              billing.purchaseCallback(topic);
            }
          } 
        }, getInstallationId());
  }
  
  @Override
  public void queryInventory(List<Topic> topicList, final IBilling billing) {
    Gdx.app.log(ScienceEngine.LOG, "Querying inventory.");
    if ((ScienceEngine.DEV_MODE & DevMode.BILLING_DUMMY) == 1) {
      super.queryInventory(topicList, billing);
      return;
    }
    List<String> productList = new ArrayList<String>();
    for (Topic topic: topicList) {
      productList.add(topic.toProductId());
    }
    
    iabHelper.queryInventoryAsync(true, productList,
        new IabHelper.QueryInventoryFinishedListener() {
          @Override
          public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
              Gdx.app.error(ScienceEngine.LOG, "Failed to query inventory: " + result);
              billing.inventoryCallback(null);
            }
            Gdx.app.log(ScienceEngine.LOG, "Query inventory was successful.");
            billing.inventoryCallback(inventory);
          }
        });    
  }
  
  // Listener that's called when we finish querying the items and subscriptions we own
  IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
      public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
        Gdx.app.log(ScienceEngine.LOG, "Query inventory finished.");
          if (result.isFailure()) {
            Gdx.app.log(ScienceEngine.LOG, "Failed to query inventory: " + result);
            return;
          }

          Gdx.app.log(ScienceEngine.LOG, "Query inventory was successful.");
      }
  };

  /** Verifies the developer payload of a purchase. */
  boolean verifyDeveloperPayload(Purchase purchase, Topic topic) {
    if (purchase == null) {
      Gdx.app.error(ScienceEngine.LOG, "Error developer payload - purchase missing");
      return false;
    }
    String payload = purchase.getDeveloperPayload();
    
    if (topic != Topic.fromProductId(purchase.getProductId())) {
      Gdx.app.error(ScienceEngine.LOG, "Error developer payload - topic product mismatch: " + 
          topic + " <> " + purchase.getProductId());
      return false;
    }
    
    /*
     * Verify that the developer payload of the purchase is correct. It will be
     * the same one that is sent when initiating the purchase.
     * 
     * WARNING: Locally generating a random string when starting a purchase and 
     * verifying it here might seem like a good approach, but this will fail in the 
     * case where the user purchases an item on one device and then uses your app on 
     * a different device, because on the other device you will not have access to the
     * random string you originally generated.
     *
     * So a good developer payload has these characteristics:
     * 
     * 1. If two different users purchase an item, the payload is different between them,
     *    so that one user's purchase can't be replayed to another user.
     * 
     * 2. The payload must be such that you can verify it even when the app wasn't the
     *    one who initiated the purchase flow (so that items purchased by the user on 
     *    one device work on other devices owned by the user).
     * 
     * Using your own server to store and verify developer payloads across app
     * installations is recommended.
     */
    
    if (!payload.equals(getInstallationId())) {
      Gdx.app.error(ScienceEngine.LOG, "Error developer payload - payload installid mismatch: " + 
          payload + " <> " + getInstallationId());
      return false;      
    }
    
    return true;
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