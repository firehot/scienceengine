package com.mazalearn.gwt.client;

import java.io.IOException;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
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
    int k = 0;
    for (int j = 0; j < pixmap.getHeight(); j++) {
      for (int i = 0; i < pixmap.getWidth(); i++) {
        int pixel = pixmap.getPixel(i, j); // RGBA
        lines[k++] = (byte) ((pixel >> 24) & 0xFF);
        lines[k++] = (byte) ((pixel >> 16) & 0xFF);
        lines[k++] = (byte) ((pixel >> 8) & 0xFF);
        lines[k++] = (byte) ((pixel) & 0xFF);
      }
    }
  }

  @Override
  public void putBytes(Pixmap pixmap, byte[] lines) {
    Pixmap.setBlending(Blending.None);
    int k = 0;
    for (int j = 0; j < pixmap.getHeight(); j++) {
      for (int i = 0; i < pixmap.getWidth(); i++) {
        int pixel = (lines[k] << 24) | (lines[k+1] << 16) | (lines[k+2] << 8) | (lines[k+3]);
        pixmap.drawPixel(i, j, pixel);
        k += 4;
      }
    }
  }

  @Override
  public byte[] getPngBytes(Pixmap snapshot) {
    try {
      return PngWriter.write(snapshot);
    } catch (IOException e) {
      e.printStackTrace();
      return new byte[0];
    }
  }
}