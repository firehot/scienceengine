package com.mazalearn.gwt.server;

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;

@SuppressWarnings("serial")
public class UploadServlet extends HttpServlet {

  private static final String COACH_IMAGE = "coach";

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    System.out.println("Received post: " + request.getContentLength());
    response.getWriter().append("Post received");
    String userEmail = request.getHeader("User");
    System.out.println("User: " + userEmail);
    BufferedInputStream dis = new BufferedInputStream(request.getInputStream());
    byte[] pngImage = new byte[request.getContentLength()];
    dis.read(pngImage);
    saveUserImage(userEmail, pngImage);
    dis.close();
  }

  public void saveUserImage(String userEmail, byte[] pngImage) throws IllegalStateException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Key key = KeyFactory.createKey(User.class.getSimpleName(), userEmail);
    Entity entity;
    try {
      entity = ds.get(key);
    } catch (EntityNotFoundException e) {
      entity = new Entity(User.class.getSimpleName(), userEmail);
      ds.put(entity);
    }
    entity.setProperty(COACH_IMAGE, new Blob(pngImage));
    ds.put(entity);
    System.out.println("User " + userEmail + " saved image");
  }

}
