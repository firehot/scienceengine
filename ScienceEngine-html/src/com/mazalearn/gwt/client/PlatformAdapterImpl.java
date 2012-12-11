package com.mazalearn.gwt.client;

import java.io.File;

import com.google.gwt.user.client.Window;
import com.mazalearn.scienceengine.AbstractPlatformAdapter;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.data.GwtMessages;

class PlatformAdapterImpl extends AbstractPlatformAdapter {
  
  IMessage messages;
  
  @Override
  public Platform getPlatform() {
    return Platform.GWT;
  }
  
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
  public IMessage getMsg() {
    if (messages == null) {
      this.messages = new GwtMessages(Platform.Desktop);
    }
    return messages;
  }
}