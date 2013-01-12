package com.mazalearn.gwt.client;

import java.io.File;
import java.nio.Buffer;

import com.badlogic.gdx.graphics.Pixmap;
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
  public void showExternalURL(String url) {
    browseURL(url);
  }

  @Override
  public boolean playVideo(File file) {
    return false;
  }

  @Override
  public void getBytes(Pixmap pixmap, byte[] lines) {
    Buffer pixels = pixmap.getPixels();
    pixels.clear();
//    pixels.get(lines);
  }

  @Override
  public void setBytes(Pixmap pixmap, byte[] lines) {
    Buffer pixels = pixmap.getPixels();
    pixels.clear();
    // pixels.put(lines);
    pixels.clear();   
  }
}