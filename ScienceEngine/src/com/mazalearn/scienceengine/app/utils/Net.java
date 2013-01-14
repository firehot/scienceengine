package com.mazalearn.scienceengine.app.utils;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.Protocol;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mazalearn.scienceengine.ScienceEngine;

public class Net {

  public static void httpPost(String path, String contentType, byte[] data) {
    Socket socket = Gdx.net.newClientSocket(Protocol.TCP, "localhost", 8888, null);
    try {
      DataOutputStream wr = 
          new DataOutputStream(socket.getOutputStream());
      wr.writeBytes("POST " + path + " HTTP/1.0\r\n");
      wr.writeBytes("Content-Length: " + data.length + "\r\n");
      wr.writeBytes("Content-Type: " + contentType + "\r\n");
      wr.writeBytes("User: " + "sridhar.sundaram@gmail.com" + "\r\n");
      wr.writeBytes("\r\n");
      //URLEncoder.encode()
      wr.write(data, 0, data.length);
      wr.flush();
      //wr.close();
      Gdx.app.log(ScienceEngine.LOG, "Posted to " + path + " bytes: " + data.length);
      byte[] response = new byte[1000];
      socket.getInputStream().read(response);
      Gdx.app.log(ScienceEngine.LOG, "Response " + new String(response));
      wr.close();
    } catch (IOException e) {
      throw new GdxRuntimeException(e);
    }
  }
}
