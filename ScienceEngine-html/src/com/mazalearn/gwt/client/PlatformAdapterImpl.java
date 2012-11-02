package com.mazalearn.gwt.client;

import java.io.File;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.google.gwt.user.client.Window;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.app.utils.PlatformAdapter;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.data.GwtMessages;

class PlatformAdapterImpl implements PlatformAdapter {
  
  IMessage messages;
  
  @Override
  public void browseURL(String url) {
    Window.open(url, "_blank", "");
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