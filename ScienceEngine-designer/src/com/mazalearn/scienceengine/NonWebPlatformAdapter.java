package com.mazalearn.scienceengine;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.designer.LevelEditor;
import com.mazalearn.scienceengine.designer.PngWriter;

public class NonWebPlatformAdapter extends AbstractPlatformAdapter {
  
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
  public byte[] getPngBytes(Pixmap snapshot) {
    try {
      return PngWriter.generateImage(snapshot);
    } catch (IOException e) {
      throw new GdxRuntimeException(e);
    }
  }
  
  @Override
  public void httpPost(String path, String contentType, Map<String, String> params, byte[] data) {
    String hostPort = ScienceEngine.getHostPort();
    String host = hostPort.substring(0, hostPort.indexOf(":"));
    int port = Integer.parseInt(hostPort.substring(hostPort.indexOf(":") + 1));
    try {
      Socket socket = Gdx.net.newClientSocket(Protocol.TCP, host, port, null);
      if (socket == null) {
        Gdx.app.log(ScienceEngine.LOG, "Could not open socket to " + hostPort + "/" + path);
        return;
      }
      DataOutputStream wr = 
          new DataOutputStream(socket.getOutputStream());
      wr.writeBytes("POST " + path + " HTTP/1.0\r\n");
      wr.writeBytes("Host: " + host + "\r\n");
      wr.writeBytes("Content-Length: " + data.length + "\r\n");
      wr.writeBytes("Content-Type: " + contentType + "\r\n");
      for (Map.Entry<String, String> entry: params.entrySet()) {
        wr.writeBytes(entry.getKey() + ": " + entry.getValue() + "\r\n");
      }
      wr.writeBytes("\r\n");
      wr.write(data, 0, data.length);
      wr.flush();
      Gdx.app.log(ScienceEngine.LOG, "Posted to " + path + " bytes: " + data.length);
      byte[] response = new byte[1000];
      socket.getInputStream().read(response);
      Gdx.app.log(ScienceEngine.LOG, "Response " + new String(response));
      wr.close();
    } catch (Exception e) {
      Gdx.app.log(ScienceEngine.LOG, "Could not upload to " + hostPort + "/" + path);
      e.printStackTrace();
      throw new GdxRuntimeException(e);
    }
  }

}