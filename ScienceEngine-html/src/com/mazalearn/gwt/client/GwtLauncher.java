package com.mazalearn.gwt.client;


import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.google.gwt.user.client.Window;
import com.mazalearn.scienceengine.Device;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter.Platform;

public class GwtLauncher extends GwtApplication {
  @Override
	public GwtApplicationConfiguration getConfig () {
		GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(800,480);
		return cfg;
	}

	@Override
	public ApplicationListener getApplicationListener () {
	  String href = Window.Location.getHref().replace("/scienceengine#", "");
    ScienceEngine.setPlatformAdapter(new GwtPlatformAdapter(Platform.GWT));
    ScienceEngine scienceEngine = new ScienceEngine(href, Device.Desktop);
		ScienceEngine.DEV_MODE.setDebug(false);
    return scienceEngine;
	}
}
 