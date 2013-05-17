package com.mazalearn.scienceengine;


import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.util.Log;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter.Platform;
import com.mazalearn.scienceengine.billing.util.IabHelper;
import com.mazalearn.scienceengine.billing.util.IabResult;
import com.mazalearn.scienceengine.billing.util.Inventory;
import com.mazalearn.scienceengine.billing.util.Purchase;
import com.mazalearn.scienceengine.billing.util.Security;

public class MainActivity extends AndroidApplication {

  protected static final String TAG = "MainActivity";
  protected static final String SKU_ELECTROMAGNETISM = "electromagnetism";
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
          Log.d(TAG, "Problem setting up In-app Billing: " + result);
          return;
        }            
        Log.d(TAG, "Setup successful. Querying inventory.");
        iabHelper.queryInventoryAsync(true, Arrays.asList(new String[] {SKU_ELECTROMAGNETISM}), mGotInventoryListener);
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
    PlatformAdapterImpl platformAdapter = new PlatformAdapterImpl(this, platform);
    
    scienceEngine.setPlatformAdapter(platformAdapter);
    initialize(scienceEngine, cfg);
  }
   
  String findAndroidId() {
    return Settings.Secure.getString(getContentResolver(), Secure.ANDROID_ID);
  }
  // Listener that's called when we finish querying the items and subscriptions we own
  IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
      public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
          Log.d(TAG, "Query inventory finished.");
          if (result.isFailure()) {
            Log.e(TAG, "Failed to query inventory: " + result);
            return;
          }

          Log.d(TAG, "Query inventory was successful.");
          
          /*
           * Check for items we own. Notice that for each purchase, we check
           * the developer payload to see if it's correct! See
           * verifyDeveloperPayload().
           */
          
          // Do we have the electromagnetism upgrade?
          Purchase electromagnetismPurchase = inventory.getPurchase(SKU_ELECTROMAGNETISM);
          boolean mHasElectromagnetism = (electromagnetismPurchase != null && verifyDeveloperPayload(electromagnetismPurchase));
          Log.d(TAG, "User " + (mHasElectromagnetism ? "has electromagnetism" : "does not have electromagnetism"));
          
          Log.d(TAG, "Initial inventory query finished");
      }
  };

  IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
      public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
         if (result.isFailure()) {
            Log.d(TAG, "Error purchasing: " + result);
            return;
         }      
         if (!verifyDeveloperPayload(purchase)) {
           Log.e(TAG, "Error purchasing. Authenticity verification failed.");
           return;
         }
         Log.d(TAG, "Purchase successful.");
         if (purchase.getSku().equals(SKU_ELECTROMAGNETISM)) {
            // Give user access to electromagnetism
         }
      }
    };

  void purchaseElectromagnetism(String userId) {
    iabHelper.launchPurchaseFlow(this, SKU_ELECTROMAGNETISM, 10001,   
        mPurchaseFinishedListener, userId);    
  }
  
  /** Verifies the developer payload of a purchase. */
  boolean verifyDeveloperPayload(Purchase p) {
      String payload = p.getDeveloperPayload();
      
      /*
       * TODO: verify that the developer payload of the purchase is correct. It will be
       * the same one that you sent when initiating the purchase.
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
      
      return true;
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