package com.mazalearn.scienceengine.client;

import java.io.File;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.app.utils.PlatformAdapter;
import com.mazalearn.scienceengine.app.utils.PlatformAdapter.Platform;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.data.GwtMessages;

public class GwtLauncher extends GwtApplication {
	@Override
	public GwtApplicationConfiguration getConfig () {
		GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(960,640);
		return cfg;
	}

	@Override
	public ApplicationListener getApplicationListener () {
		ScienceEngine scienceEngine = new ScienceEngine();
		scienceEngine.setPlatformAdapter(new PlatformAdapterImpl());
		scienceEngine.DEV_MODE = DevMode.PRODUCTION;
    return scienceEngine;
	}
  static class PlatformAdapterImpl implements PlatformAdapter {
    
    IMessage messages;
    
    @Override
    public void browseURL(String url) {
    }

    @Override
    public void showURL(String url) {
      browseURL(url);
    }

    @Override
    public boolean playVideo(File file) {
      return false;
    }


    @Override
    public Stage createLevelEditor(IScience2DController science2DController,
        AbstractScreen screen) {
      return null;
    }
    
    @Override
    public IMessage getMsg() {
      if (messages == null) {
        this.messages = new GwtMessages(Platform.Desktop);
      }
      return messages;
    }
  }
}
 