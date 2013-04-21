package com.mazalearn.gwt.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.mazalearn.scienceengine.app.services.ProfileData;

@SuppressWarnings("serial")
public class UserPermissionServlet extends HttpServlet {

  private static final String PERMISSIONS = "permissions";

  public static boolean checkUserPermitted(String userId) throws IllegalStateException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Key key = KeyFactory.createKey(User.class.getSimpleName(), userId);
    Entity entity;
    try {
      entity = ds.get(key);
    } catch (EntityNotFoundException e) {
      entity = new Entity(User.class.getSimpleName(), userId);
      ds.put(entity);
    }
    String permissions = (String) entity.getProperty(PERMISSIONS);
    System.out.println("User " + userId + " permissions: " + permissions);
    return (permissions != null && permissions.contains("demo"));
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String userId = request.getParameter(ProfileData.USER_ID);
    String permissions = request.getParameter(PERMISSIONS);
    if (userId == null || permissions == null) {
      response.getWriter().append("userId or permissions not found");
      return;
    }
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Key key = KeyFactory.createKey(User.class.getSimpleName(), userId);
    Entity entity;
    try {
      entity = ds.get(key);
    } catch (EntityNotFoundException e) {
      entity = new Entity(User.class.getSimpleName(), userId);
    }
    entity.setProperty(PERMISSIONS, "demo");
    ds.put(entity);
    
    response.getWriter().append("User: <" + userId + "> granted permissions: " + permissions);
  }
}
