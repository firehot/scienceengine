using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Security.Cryptography;
using java.nio;
using java.util;

using MonoTouch.AVFoundation;
using MonoTouch.Foundation;
using MonoTouch.UIKit;

using com.badlogic.gdx;
using com.badlogic.gdx.backends.ios;
using com.badlogic.gdx.graphics;
using com.mazalearn.scienceengine;
using com.mazalearn.scienceengine.designer;
using com.mazalearn.scienceengine.app.services;
using com.mazalearn.scienceengine.app.utils;

namespace scienceengineios {

	public class Sha1Implementor : Crypter.Sha1 {
		public byte[] sha1Hash (byte[] toHash) {
			HashAlgorithm sha1 = HashAlgorithm.Create("SHA1");
			return sha1.ComputeHash(toHash);
		}	
	}

	public class Application
	{
		[Register ("AppDelegate")]
		public partial class AppDelegate : IOSApplication {
			static UIWindow webViewWindow;
			static ScienceEngine scienceEngine;
			static IosPlatformAdapter iosAdapter;
			static WebViewController webViewController;
			static UINavigationController navigationController;

			public AppDelegate(): base(getScienceEngine(), getConfig()) {
			}
			public override bool FinishedLaunching (UIApplication app, NSDictionary options) {
				bool result = base.FinishedLaunching(app, options);

				// create a new window instance based on the screen size
				webViewWindow = new UIWindow (UIScreen.MainScreen.Bounds);
				webViewController = new WebViewController(app.KeyWindow);
				
				navigationController = new UINavigationController();
				navigationController.PushViewController (webViewController, true);

				// If you have defined a view, add it here:
				//webViewWindow.AddSubview (navigationController.View);
				webViewWindow.RootViewController = navigationController;
				iosAdapter.setWindowAndWebViewController(webViewWindow, webViewController);
				return result;
			}
			internal static ScienceEngine getScienceEngine () {
				ScienceEngine.DEV_MODE = ScienceEngine.DevMode.PRODUCTION | ScienceEngine.DevMode.BILLING_DUMMY;
				scienceEngine = new ScienceEngine ("", Device.IPad);
				iosAdapter = new IosPlatformAdapter();
				ScienceEngine.setPlatformAdapter(iosAdapter);
				Crypter.setSha1Implementor(new Sha1Implementor());
				return scienceEngine;
			}
			internal static IOSApplicationConfiguration getConfig() {
				IOSApplicationConfiguration config = new IOSApplicationConfiguration();
				config.orientationLandscape = true;
				config.orientationPortrait = false;
				config.useAccelerometer = false;
				config.useObjectAL = false;
				config.preventScreenDimming = false;
				config.displayScaleLargeScreenIfRetina = 0.5f;
				config.useMonotouchOpenTK = true;
				return config;
			}
		}
		
		static void Main (string[] args)
		{
			UIApplication.Main (args, null, "AppDelegate");
		}
	}
}
