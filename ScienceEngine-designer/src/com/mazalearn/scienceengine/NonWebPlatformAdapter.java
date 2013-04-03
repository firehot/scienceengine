package com.mazalearn.scienceengine;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.designer.LevelEditor;
import com.mazalearn.scienceengine.designer.PngWriter;

public class NonWebPlatformAdapter extends AbstractPlatformAdapter {
  
  private static final String BODY_DELIMITER = "\r\n\r\n";
  private static byte[] responseBytes = new byte[40000];

  public NonWebPlatformAdapter(Platform platform) {
    super(platform);
  }

  @Override
  public Stage createLevelEditor(IScience2DController science2DController,
      AbstractScreen screen) {
    return new LevelEditor(science2DController, screen);
  }

  @Override
  public void getBytes(Pixmap pixmap, byte[] lines) {
    ByteBuffer pixels = pixmap.getPixels();
    pixels.get(lines);
  }

  @Override
  public void putBytes(Pixmap pixmap, byte[] lines) {
    ByteBuffer pixels = pixmap.getPixels();
    pixels.clear();
    pixels.put(lines);
    pixels.clear();   
  }
  
  @Override
  public byte[] pixmap2Bytes(Pixmap snapshot) {
    try {
      return PngWriter.generateImage(snapshot);
    } catch (IOException e) {
      throw new GdxRuntimeException(e);
    }
  }
  
  @Override 
  public Pixmap bytes2Pixmap(byte[] bytes) {
    try {
      return new Pixmap(new Gdx2DPixmap(bytes, 0, bytes.length, 0));
    } catch (IOException e) {
      if (ScienceEngine.DEV_MODE == DevMode.DEBUG) e.printStackTrace();
      return null;
    }
  }
  
  @Override
  public String httpPost(String path, String contentType, Map<String, String> params, byte[] data) {
    String hostPort = ScienceEngine.getHostPort();
    String host = hostPort.substring(0, hostPort.indexOf(":"));
    int port = Integer.parseInt(hostPort.substring(hostPort.indexOf(":") + 1));
    Socket socket = null;
    try {
      socket = Gdx.net.newClientSocket(Protocol.TCP, host, port, null);
      if (socket == null) {
        Gdx.app.log(ScienceEngine.LOG, "Could not open socket to " + hostPort + path);
        return null;
      }
      DataOutputStream wr = 
          new DataOutputStream(socket.getOutputStream());
      String header = makeHeaderString(path, contentType, params, data.length, host);
      wr.writeBytes(header);
      wr.write(data, 0, data.length);
      wr.flush();
      Gdx.app.log(ScienceEngine.LOG, "Posted to " + path + " bytes: " + data.length);
      String responseStr = getResponseBody(socket.getInputStream());
      wr.close();
      return responseStr;
    } catch (Exception e) {
      Gdx.app.log(ScienceEngine.LOG, "Could not upload to " + hostPort + path);
      if (ScienceEngine.DEV_MODE == DevMode.DEBUG) e.printStackTrace();
      throw new GdxRuntimeException(e);
    } finally {
      if (socket != null) {
        socket.dispose();
      }
    }
  }

  @Override
  public String httpGet(String path) {
    String hostPort = ScienceEngine.getHostPort();
    String host = hostPort.substring(0, hostPort.indexOf(":"));
    int port = Integer.parseInt(hostPort.substring(hostPort.indexOf(":") + 1));
    Socket socket = null;
    try {
      socket = Gdx.net.newClientSocket(Protocol.TCP, host, port, null);
      if (socket == null) {
        Gdx.app.log(ScienceEngine.LOG, "Could not open socket to " + hostPort + path);
        return null;
      }
      DataOutputStream wr = 
          new DataOutputStream(socket.getOutputStream());
      wr.writeBytes("GET " + hostPort + path + " HTTP/1.0\r\n\r\n");
      wr.flush();
      Gdx.app.log(ScienceEngine.LOG, "GET " + hostPort + path);
      String responseStr = getResponseBody(socket.getInputStream());
      wr.close();
      return responseStr;
    } catch (Exception e) {
      Gdx.app.log(ScienceEngine.LOG, "Could not get " + hostPort + path);
      if (ScienceEngine.DEV_MODE == DevMode.DEBUG) e.printStackTrace();
      return "";
    } finally {
      if (socket != null) {
        socket.dispose();
      }
    }
  }
  
  // NOt thread safe because of responseBytes
  private String getResponseBody(InputStream inputStream) throws IOException {
    int numBytes = 0;
    int count;
    while ((count = inputStream.read(responseBytes, numBytes, 1024)) > 0) {
      numBytes += count;
      Gdx.app.debug(ScienceEngine.LOG, "Numbytes read: " + numBytes);
    }
    String responseStr = new String(responseBytes, 0, numBytes, "US-ASCII");
    Gdx.app.log(ScienceEngine.LOG, "Response received: " + numBytes);
    String firstLine = responseStr.substring(0, responseStr.indexOf("\n"));
    if (!firstLine.contains("200")) {
      throw new IllegalStateException("Improper HTTP response:\n" + responseStr);
    }
    int pos = responseStr.indexOf(BODY_DELIMITER);
    if (pos == -1) return "";
    responseStr = responseStr.substring(pos).replace("\r\n", "").trim();
    Gdx.app.log(ScienceEngine.LOG, "Response data length = " + responseStr.length());
    return responseStr;
  }

  private String makeHeaderString(String path, String contentType,
      Map<String, String> params, int length, String host) {
    StringBuffer s = new StringBuffer();
    s.append("POST " + path + " HTTP/1.0\r\n");
    s.append("Host: " + host + "\r\n");
    s.append("Content-Length: " + length + "\r\n");
    s.append("Content-Type: " + contentType + "\r\n");
    for (Map.Entry<String, String> entry: params.entrySet()) {
      s.append(entry.getKey() + ": " + entry.getValue() + "\r\n");
    }
    s.append("\r\n");
    return s.toString();
  }

  @Override
  public void takeSnapshot(Stage stage, Topic topic, Topic level, int x, int y, int width, int height) {
    LevelEditor.takeSnapshot(stage, topic, level, x, y, width, height);
  }

  @Override
  public String getInstallationId() {
    return Installation.id();
  }

  @Override
  public void executeAsync(Runnable runnable) {
    Thread syncThread = new Thread(runnable, "syncthread");
    syncThread.start();
  }
}