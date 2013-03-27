package com.mazalearn.gwt.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

@SuppressWarnings("serial")
public class UserProfileDeleteServlet extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    System.out.println("Received get: " + request.getContentLength());
    String userEmail = request.getParameter("User");
    System.out.println("User: " + userEmail);
    deleteUserProfile(userEmail);
    response.getWriter().append("User Profile deleted");
  }

  public void deleteUserProfile(String userEmail) throws IllegalStateException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity user = ProfileServlet.retrieveUser(userEmail, ds);
    if (user != null) {
      user.setProperty(ProfileServlet.PROFILE, null);
      ds.put(user);      
    }
  }

}
