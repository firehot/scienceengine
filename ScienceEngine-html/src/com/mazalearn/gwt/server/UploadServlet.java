package com.mazalearn.gwt.server;

import java.io.BufferedInputStream;
import java.io.IOException;

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

  private static final String NAME = "name";
  private static final String COACH_IMAGE = "coach";
  private static final String CURRENT = "current";
  private static final String COLOR = "color";

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    System.out.println("Received post: " + request.getContentLength());
    response.getWriter().append("Post received");
    String userEmail = request.getHeader("User");
    String userName = request.getHeader("UserName");
    String current = request.getHeader("Current");
    String color = request.getHeader("color");
    System.out.println("User: " + userEmail + " current: " + current + " color:" + color);
    BufferedInputStream bis = new BufferedInputStream(request.getInputStream());
    byte[] pngImage = new byte[request.getContentLength()];
    bis.read(pngImage);
    saveUserImage(userEmail, userName, pngImage, current, color);
    bis.close();
  }

  public void saveUserImage(String userEmail, String userName, byte[] pngImage, 
      String current, String color) 
      throws IllegalStateException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Key key = KeyFactory.createKey(User.class.getSimpleName(), userEmail);
    Entity entity;
    try {
      entity = ds.get(key);
    } catch (EntityNotFoundException e) {
      entity = new Entity(User.class.getSimpleName(), userEmail);
      ds.put(entity);
    }
    entity.setProperty(NAME, userName);
    entity.setProperty(COACH_IMAGE, new Blob(pngImage));
    entity.setProperty(CURRENT, Float.parseFloat(current));
    entity.setProperty(COLOR, color);
    ds.put(entity);
    System.out.println("User " + userEmail + " saved image");
  }

}
