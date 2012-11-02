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

@SuppressWarnings("serial")
public class UserPermissionServlet extends HttpServlet {

  private static final String PERMISSIONS = "permissions";

  public static boolean checkUserPermitted(String userEmail) throws IllegalStateException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Key key = KeyFactory.createKey(User.class.getSimpleName(), userEmail);
    Entity entity;
    try {
      entity = ds.get(key);
    } catch (EntityNotFoundException e) {
      entity = new Entity(User.class.getSimpleName(), userEmail);
      ds.put(entity);
    }
    String permissions = (String) entity.getProperty(PERMISSIONS);
    System.out.println("User " + userEmail + " permissions: " + permissions);
    return (permissions != null && permissions.contains("demo"));
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String userEmail = request.getParameter("userEmail");
    String permissions = request.getParameter(PERMISSIONS);
    if (userEmail == null || permissions == null) {
      response.getWriter().append("userEmail or permissions not found");
      return;
    }
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Key key = KeyFactory.createKey(User.class.getSimpleName(), userEmail);
    Entity entity;
    try {
      entity = ds.get(key);
    } catch (EntityNotFoundException e) {
      entity = new Entity(User.class.getSimpleName(), userEmail);
    }
    entity.setProperty(PERMISSIONS, "demo");
    ds.put(entity);
    
    response.getWriter().append("User: <" + userEmail + "> granted permissions: " + permissions);
  }
}
