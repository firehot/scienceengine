using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;

using MonoTouch.Foundation;
using MonoTouch.UIKit;

using com.badlogic.gdx.backends.ios;
using com.mazalearn.scienceengine;

namespace scienceengineios
{		
	public class IosPlatformAdapter : AbstractPlatformAdapter {
		UIWindow window;
		WebViewController webViewController;

		public IosPlatformAdapter (UIWindow window, WebViewController webViewController): base( ) {
			this.window = window;
			this.webViewController = webViewController;
		}
		public override void browseURL (string url) {
			string contentDirectoryPath = Path.Combine (NSBundle.MainBundle.BundlePath, "Content/");
			webViewController.load(url);
		//	webView.ScalesPageToFit = true;
			// make the window visible
			window.MakeKeyAndVisible ();
		}
	}

	public class Application
	{
		[Register ("AppDelegate")]
		public partial class AppDelegate : IOSApplication {
			static UIWindow webViewWindow;
			static ScienceEngine scienceEngine;
			static WebViewController webViewController;
			static UINavigationController navigationController;

			public AppDelegate(): base(getScienceEngine(), getConfig()) {
			}
			public override bool FinishedLaunching (UIApplication app, NSDictionary options) {
				bool result = base.FinishedLaunching(app, options);

				// create a new window instance based on the screen size
				webViewWindow = new UIWindow (UIScreen.MainScreen.Bounds);
				/*UIWebView webView = new UIWebView(UIScreen.MainScreen.Bounds);
				window.AddSubview(webView);
				scienceEngine.setPlatformAdapter (new IosPlatformAdapter(window, webView));

				return result;*/
				webViewController = new WebViewController(app.KeyWindow);
				
				navigationController = new UINavigationController();
				navigationController.PushViewController (webViewController, true);

				// If you have defined a view, add it here:
				//webViewWindow.AddSubview (navigationController.View);
				webViewWindow.RootViewController = navigationController;
				scienceEngine.setPlatformAdapter (new IosPlatformAdapter(webViewWindow, webViewController));
				return result;
			}
			internal static ScienceEngine getScienceEngine () {
				scienceEngine = new ScienceEngine ("");
				return scienceEngine;
			}
			internal static IOSApplicationConfiguration getConfig() {
				IOSApplicationConfiguration config = new IOSApplicationConfiguration();
				config.orientationLandscape = true;
				config.orientationPortrait = false;
				config.useAccelerometer = false;
				return config;
			}
		}
		
		static void Main (string[] args)
		{
			UIApplication.Main (args, null, "AppDelegate");
		}
	}
}
