package com.mazalearn.gwt.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
public class UserImageServlet extends HttpServlet {

  private static final String COACH_IMAGE = "coach";

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    System.out.println("Received get: " + request.getContentLength());
    String userEmail = request.getParameter("User");
    System.out.println("User: " + userEmail);
    response.setHeader("ContentType", "image/png");
    BufferedOutputStream dis = new BufferedOutputStream(response.getOutputStream());
    Blob userImage = getUserImage(userEmail);
    dis.write(userImage.getBytes());
    dis.close();
  }

  public Blob getUserImage(String userEmail) throws IllegalStateException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Key key = KeyFactory.createKey(User.class.getSimpleName(), userEmail);
    Entity entity;
    try {
      entity = ds.get(key);
    } catch (EntityNotFoundException e) {
      return null;
    }
    return (Blob) entity.getProperty(COACH_IMAGE);
  }

}
