package com.mazalearn.gwt.client;

import java.io.IOException;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.mazalearn.scienceengine.AbstractPlatformAdapter;
import com.mazalearn.scienceengine.ScienceEngine;

class GwtPlatformAdapter extends AbstractPlatformAdapter {
  
  public GwtPlatformAdapter(Platform platform) {
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
        int pixel = (int) lines[k++] & 0xFF;
        pixel <<= 8;
        pixel |= (int) lines[k++] & 0xFF;
        pixel <<= 8;
        pixel |= (int) lines[k++] & 0xFF;
        pixel <<= 8;
        pixel |= (int) lines[k++] & 0xFF;
        // TODO: Bug in Gwt PixMap - setColor uses format ARGB instead of RGBA
        setColor(pixmap, pixel);
        pixmap.drawPixel(i, j);
      }
    }
  }

  private void setColor(Pixmap pixmap, int color) {
    float a = (color & 0xff) / 255f;
    float r = ((color >>> 24) & 0xff) / 255f;
    float g = ((color >>> 16) & 0xff) / 255f;
    float b = ((color >>> 8) & 0xff) / 255f;
    pixmap.setColor(r, g, b, a);
  }

  @Override
  public byte[] pixmap2Bytes(Pixmap snapshot) {
    try {
      return PngWriter.write(snapshot);
    } catch (IOException e) {
      e.printStackTrace();
      return new byte[0];
    }
  }
  
  @Override
  public boolean supportsLanguage() {
    return false;
  }
  
  @Override
  public String httpPost(String path, String contentType, Map<String, String> params, byte[] data) {
    String url = ScienceEngine.getHostPort() + "/" + path;
    RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, url);
    try {
      builder.setHeader("Content-Type", contentType); // "application/x-www-form-urlencoded");
      for (Map.Entry<String, String> entry: params.entrySet()) {
        builder.setHeader(entry.getKey(), entry.getValue());
      }
      String d = new String(data); // ???? should base64encode
      builder.sendRequest(d, new RequestCallback() {

        @Override
        public void onError(Request request, Throwable exception) {
        }

        @Override
        public void onResponseReceived(Request request, Response response) {
        }
      });
    } catch (RequestException e) {
      Gdx.app.log(ScienceEngine.LOG, "Could not upload to " + url);
      e.printStackTrace();
      throw new GdxRuntimeException(e);
    }
    return null;
  }
  
  @Override
  public String httpGet(String path) {
    String url = ScienceEngine.getHostPort() + "/" + path;
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
    try {
      builder.sendRequest("", new RequestCallback() {

        @Override
        public void onError(Request request, Throwable exception) {
        }

        @Override
        public void onResponseReceived(Request request, Response response) {
        }
      });
    } catch (RequestException e) {
      Gdx.app.log(ScienceEngine.LOG, "Could not get " + url);
      e.printStackTrace();
      throw new GdxRuntimeException(e);
    }
    return null;
  }
  
  @Override
  public String getInstallationId() {
    return ScienceEngine.getUserEmail(); // Fallback to user being the device for GWT
  }

  @Override
  public Pixmap bytes2Pixmap(byte[] pngBytes) {
    return null; // Dont know how to do this for GWT
  }

  @Override
  public void executeAsync(Runnable runnable) {
    /*
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      public void execute() {
         .. code here is executed using the timer technique.
      }
    });  */
  }

  @Override
  public boolean supportsSync() {
    return false;
  }

  @Override
  public String getDeviceId() {
    return "Gwt";
  }
}