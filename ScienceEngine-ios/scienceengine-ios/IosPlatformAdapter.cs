using System;
using System.Collections.Generic;
using System.IO;

using MonoTouch.AVFoundation;
using MonoTouch.Foundation;
using MonoTouch.StoreKit;
using MonoTouch.UIKit;

using com.mazalearn.scienceengine;
using com.mazalearn.scienceengine.billing;
using com.mazalearn.scienceengine.designer;
using com.mazalearn.scienceengine.app.utils;

namespace scienceengineios {

  public class IosPlatformAdapter : NonWebPlatformAdapter {
    UIWindow window;
    WebViewController webViewController;
    List<string> products;
    NSObject priceObserver, requestObserver, failedObserver;
    TextToSpeech textToSpeech;
    AVAudioPlayer audioPlayer;
    
    InAppPurchaseManager iap;
    
    public IosPlatformAdapter (): base(IPlatformAdapter.Platform.IOS) {
       iap = new InAppPurchaseManager ();
    }
    
    public void setWindowAndWebViewController(UIWindow window, WebViewController webViewController) {
      this.window = window;
      this.webViewController = webViewController;
    }
    
    public override void browseURL (string url) {
      webViewController.load (new NSUrlRequest(new NSUrl(url)));
      window.MakeKeyAndVisible ();
    }
    
    public override void showInternalURL(string url) {
      string localHtmlUrl = Path.Combine (NSBundle.MainBundle.BundlePath, url);
      webViewController.load (new NSUrlRequest(new NSUrl(localHtmlUrl, false)));
      window.MakeKeyAndVisible();
    }
    
    public override string httpPost(string path, string contentType, java.util.Map paramset, byte[] data) {
      try {
        return base.httpPost(path, contentType, paramset, data);
      } catch (System.Net.Sockets.SocketException ignore) {
        return "";
      }
    }
    
    public override string httpGet(string path) {
      try {
        return base.httpGet(path);
      } catch (System.Net.Sockets.SocketException ignore) {
        return "";
      }
    }
    
    public override void provisionSpeech() {
      if (!supportsSpeech() || textToSpeech != null) return;
      textToSpeech = new TextToSpeech ();
      textToSpeech.fliteInitFunc ();
    }
    
    public override void speak (string str, bool b) {
      if (textToSpeech == null || !ScienceEngine.getPreferencesManager().isSpeechEnabled()) return;
      str = str.Replace ("'?'", "question mark");
      byte[] bytes = textToSpeech.ConvertTextToWavStr(str);
      audioPlayer = AVAudioPlayer.FromData (NSData.FromArray(bytes));
      audioPlayer.PrepareToPlay();
      audioPlayer.Play();
    }
      
    public override bool supportsSpeech () {
      return true;
    }
  
    public override void launchPurchaseFlow(Topic sku, IBilling billing) {
      if (ScienceEngine.DEV_MODE.isDummyBilling()) {
        base.launchPurchaseFlow (sku, billing);
        return;
      }
      
      setupObserversForPurchase(sku, billing);
      iap.PurchaseProduct (sku.toProductId()); 
    }
    
    private void setupObserversForPurchase(Topic sku, IBilling billing) {
      // iap should not be null here since we first query inventory
      priceObserver = NSNotificationCenter.DefaultCenter.AddObserver (InAppPurchaseManager.InAppPurchaseManagerTransactionSucceededNotification, 
      (notification) => {
        billing.purchaseCallback(sku);
        NSNotificationCenter.DefaultCenter.RemoveObserver (priceObserver);
        NSNotificationCenter.DefaultCenter.RemoveObserver (failedObserver);
        NSNotificationCenter.DefaultCenter.RemoveObserver (requestObserver);
      });

      failedObserver = NSNotificationCenter.DefaultCenter.AddObserver (InAppPurchaseManager.InAppPurchaseManagerTransactionFailedNotification, 
      (notification) => {
        Console.WriteLine ("Transaction Failed");
        billing.purchaseCallback(null);
        NSNotificationCenter.DefaultCenter.RemoveObserver (priceObserver);
        NSNotificationCenter.DefaultCenter.RemoveObserver (failedObserver);
        NSNotificationCenter.DefaultCenter.RemoveObserver (requestObserver);
      });

      requestObserver = NSNotificationCenter.DefaultCenter.AddObserver (InAppPurchaseManager.InAppPurchaseManagerRequestFailedNotification, 
                                                                       (notification) => {
        Console.WriteLine ("Request Failed");
        billing.purchaseCallback(null);
        NSNotificationCenter.DefaultCenter.RemoveObserver (priceObserver);
        NSNotificationCenter.DefaultCenter.RemoveObserver (requestObserver);
      });
    }
      
    public override void queryInventory (java.util.List topicList, IBilling billing) {
      if (ScienceEngine.DEV_MODE.isDummyBilling()) {
        base.queryInventory (topicList, billing);
        return;
      }
     
      // only if we can make payments, request the prices
      if (iap.CanMakePayments ()) {
        // now go get prices
        products = new List<string> ();
        for (int i = 0; i < topicList.size (); i++) {
          products.Add (((Topic) topicList.get (i)).toProductId());
        }
        setupObserversForInventory(topicList, billing);
        iap.RequestProductData (products); // async request via StoreKit -> App Store
      } else {
        // can't make payments (purchases turned off in Settings?)
        billing.inventoryCallback(null);
      }
    }
    
    private void setupObserversForInventory(java.util.List topicList, IBilling billing) {
      // setup the observer to wait for prices to come back from StoreKit <- AppStore
      priceObserver = NSNotificationCenter.DefaultCenter.AddObserver (InAppPurchaseManager.InAppPurchaseManagerProductsFetchedNotification, 
     (notification) => {
        var info = notification.UserInfo;
        Inventory inventory = new Inventory();
        for (int i = 0; i < topicList.size (); i++) {
          NSString productId = new NSString(((Topic) topicList.get (i)).toProductId());
          if (!info.ContainsKey(productId)) continue;
          var product = (SKProduct)info.ObjectForKey (productId);
          
          // Title and Description intentionally flipped
          SkuDetails skuDetails = new SkuDetails(product.ProductIdentifier, "inapp", 
              product.LocalizedDescription, product.LocalizedTitle,
              product.PriceLocale.CurrencySymbol + product.Price.ToString ());
          inventory.addSkuDetails(skuDetails);
        }
        NSNotificationCenter.DefaultCenter.RemoveObserver (priceObserver);
        NSNotificationCenter.DefaultCenter.RemoveObserver (requestObserver);
        billing.inventoryCallback(inventory);
      });

      requestObserver = NSNotificationCenter.DefaultCenter.AddObserver (InAppPurchaseManager.InAppPurchaseManagerRequestFailedNotification, 
                                                                       (notification) => {
        Console.WriteLine ("Request Failed");
        billing.inventoryCallback(null);
        NSNotificationCenter.DefaultCenter.RemoveObserver (priceObserver);
        NSNotificationCenter.DefaultCenter.RemoveObserver (requestObserver);
      });
    }      
  }  // IosPlatformAdapter
}  // namespace
