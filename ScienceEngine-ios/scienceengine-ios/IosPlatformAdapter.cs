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

namespace scienceengineios
{
	public class IosPlatformAdapter : NonWebPlatformAdapter
	{
		UIWindow window;
		WebViewController webViewController;
    List<string> products;
    bool pricesLoaded = false;
    NSObject priceObserver, requestObserver;
	TextToSpeech speaker;
		AVAudioPlayer audioPlayer;
    
    InAppPurchaseManager iap;
		
		public IosPlatformAdapter (): base(IPlatformAdapter.Platform.IOS) {
		  speaker = new TextToSpeech ();
			speaker.fliteInitFunc ();
		}
		
		public void setWindowAndWebViewController(UIWindow window, WebViewController webViewController) {
			this.window = window;
			this.webViewController = webViewController;
		}
		
		public override void browseURL (string url)
		{
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
		
    public override void launchPurchaseFlow(Topic sku, IBilling billing) {
      if ((ScienceEngine.DEV_MODE & ScienceEngine.DevMode.BILLING_DUMMY) == 1) {
        base.launchPurchaseFlow (sku, billing);
        return;
      }
      // iap should not be null here since we first query inventory
      priceObserver = NSNotificationCenter.DefaultCenter.AddObserver (InAppPurchaseManager.InAppPurchaseManagerTransactionSucceededNotification, 
      (notification) => {
        billing.purchaseCallback(sku);
        NSNotificationCenter.DefaultCenter.RemoveObserver (priceObserver);
      });

      requestObserver = NSNotificationCenter.DefaultCenter.AddObserver (InAppPurchaseManager.InAppPurchaseManagerRequestFailedNotification, 
                                                                       (notification) => {
        Console.WriteLine ("Request Failed");
        billing.purchaseCallback(null);
        NSNotificationCenter.DefaultCenter.RemoveObserver (requestObserver);
      });
      
      iap.PurchaseProduct (sku.toProductId());
      
    }
   public override void speak (string str, bool b)
		{
			string basedir = Path.Combine (Environment.GetFolderPath(Environment.SpecialFolder.Personal), "..");
			string tmpdir = Path.Combine (basedir, "tmp");
			string audioFilePath = Path.Combine (tmpdir, "scienceengine.wav");
			str = str.Replace ("'?'", "question mark");
			byte[] bytes = speaker.ConvertTextToWavStr(str);
			audioPlayer = AVAudioPlayer.FromData (NSData.FromArray(bytes));
			audioPlayer.PrepareToPlay();
			audioPlayer.Play();
		}
   public override bool supportsSpeech ()
		{
			return true;
		}

   public override void queryInventory (java.util.List topicList, IBilling billing)
		{
			if ((ScienceEngine.DEV_MODE & ScienceEngine.DevMode.BILLING_DUMMY) == 1) {
				base.queryInventory (topicList, billing);
				return;
			}
			products = new List<string> ();
			for (int i = 0; i < topicList.size (); i++) {
				products.Add (((Topic) topicList.get (i)).toProductId());
		  }
			iap = new InAppPurchaseManager ();
     
			// setup the observer to wait for prices to come back from StoreKit <- AppStore
			priceObserver = NSNotificationCenter.DefaultCenter.AddObserver (InAppPurchaseManager.InAppPurchaseManagerProductsFetchedNotification, 
     (notification) => {
				var info = notification.UserInfo;
				Inventory inventory = new Inventory();
				for (int i = 0; i < topicList.size (); i++) {
					NSString productId = new NSString(((Topic) topicList.get (i)).toProductId());
					if (!info.ContainsKey(productId)) continue;
					pricesLoaded = true;
					var product = (SKProduct)info.ObjectForKey (productId);
					
					// Title and Description intentionally flipped
					SkuDetails skuDetails = new SkuDetails(product.ProductIdentifier, "inapp", 
					    product.LocalizedDescription, product.LocalizedTitle,
					    product.PriceLocale.CurrencySymbol + product.Price.ToString ());
					inventory.addSkuDetails(skuDetails);
				}
        NSNotificationCenter.DefaultCenter.RemoveObserver (priceObserver);
				billing.inventoryCallback(inventory);
			});
			// only if we can make payments, request the prices
			if (iap.CanMakePayments ()) {
				// now go get prices, if we don't have them already
				if (!pricesLoaded)
					iap.RequestProductData (products); // async request via StoreKit -> App Store
			  } else {
				  // can't make payments (purchases turned off in Settings?)
				  billing.inventoryCallback(null);
			  }
		  }
    }
}
