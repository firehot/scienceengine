using System;
using System.Drawing;
using System.IO;

using MonoTouch.UIKit;
using MonoTouch.Foundation;

namespace scienceengineios {
	/// <summary>
	/// Display web content with back/forward buttons, plus refresh and close
	/// </summary>
	public class WebViewController : UIViewController {
		UIWebView webView;
		UIToolbar navBar;
		NSUrlRequest urlRequest;
		UIBarButtonItem [] items;
		UIWindow mainWindow;

		public WebViewController (UIWindow mainWindow) : base () {
			this.mainWindow = mainWindow;
		}
		
		public override void ViewDidLoad ()
		{
			base.ViewDidLoad ();
			navBar = new UIToolbar ();
			navBar.Frame = new RectangleF (0, View.Frame.Height - 40, View.Frame.Width, 40);
			navBar.TintColor = UIColor.DarkGray;			
			
			items = new UIBarButtonItem [] {
				new UIBarButtonItem ("Back", UIBarButtonItemStyle.Bordered, (o, e) => {
				webView.GoBack (); }),
				new UIBarButtonItem ("Forward", UIBarButtonItemStyle.Bordered, (o, e) => {
				webView.GoForward (); }),
				new UIBarButtonItem (UIBarButtonSystemItem.FlexibleSpace, null),
				new UIBarButtonItem (UIBarButtonSystemItem.Refresh, (o, e) => {
				webView.Reload (); }),
				new UIBarButtonItem (UIBarButtonSystemItem.Stop, (o, e) => { 
				webView.StopLoading ();
				mainWindow.MakeKeyAndVisible ();
			})
			};
			navBar.Items = items;
			
			webView = new UIWebView ();
			webView.Frame = new RectangleF (0, 0, View.Frame.Width, View.Frame.Height - 40);
			
			webView.LoadStarted += delegate {
				UIApplication.SharedApplication.NetworkActivityIndicatorVisible = true;
				navBar.Items [0].Enabled = webView.CanGoBack;
				navBar.Items [1].Enabled = webView.CanGoForward;
			};
			webView.LoadFinished += delegate {
				UIApplication.SharedApplication.NetworkActivityIndicatorVisible = false;
				navBar.Items [0].Enabled = webView.CanGoBack;
				navBar.Items [1].Enabled = webView.CanGoForward;
			};
			
			webView.ScalesPageToFit = true;
			webView.SizeToFit ();
			load (urlRequest);

			navBar.AutoresizingMask = UIViewAutoresizing.FlexibleWidth | UIViewAutoresizing.FlexibleTopMargin;
			webView.AutoresizingMask = UIViewAutoresizing.FlexibleDimensions;
			
			View.AddSubview (webView);
			View.AddSubview (navBar);
		}

		public void load (NSUrlRequest urlRequest) {
			if (webView != null) {
				webView.LoadRequest (urlRequest);
			}
			this.urlRequest = urlRequest;
		}
		
		public override bool ShouldAutorotateToInterfaceOrientation (UIInterfaceOrientation toInterfaceOrientation) {
			return true;
		}
	}
}