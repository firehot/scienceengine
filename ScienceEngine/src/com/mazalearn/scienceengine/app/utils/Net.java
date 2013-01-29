package com.mazalearn.scienceengine.app.utils;

import java.io.DataOutputStream;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.Socket;
import com.mazalearn.scienceengine.ScienceEngine;

public class Net {

  public static void httpPost(String path, String contentType, Map<String, String> params, byte[] data) {
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
    }
  }
}
