package com.mazalearn.gwt.client;

import java.nio.Buffer;

import com.badlogic.gdx.graphics.Pixmap;
import com.google.gwt.user.client.Window;
import com.mazalearn.scienceengine.AbstractPlatformAdapter;

class PlatformAdapterImpl extends AbstractPlatformAdapter {
  
  public PlatformAdapterImpl(Platform platform) {
    super(platform);
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