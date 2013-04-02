package com.mazalearn.gwt.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;

@SuppressWarnings("serial")
public class RegistrationEmailServlet extends HttpServlet {

  static final String USER_EMAIL = "useremail";
  static final String INSTALL_ID = "installid";

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    System.out.println("Register - Received get: " + request.getContentLength());
    String userEmail = request.getParameter("e").toLowerCase();
    String installId = request.getParameter("i").toLowerCase();
    String userName = request.getParameter("n");
    long timeEmailSent = Long.parseLong(request.getParameter("t"));
    String hash = request.getParameter("h");
    System.out.println("User: " + userEmail + " id: " + installId);
    if (System.currentTimeMillis() - timeEmailSent > RegistrationServlet.EXPIRY_TIME_MS) {
      response.getWriter().append("Registration email has expired");
      return;
    }
    
    if (!RegistrationServlet.getHash(installId, userEmail, userName, timeEmailSent).equals(hash)) {
      response.getWriter().append("Invalid registration info for: " + installId + " " + userEmail);
      return;
    }
    
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    // User may exist but should not have profile
    EmbeddedEntity profile = ProfileServlet.retrieveUserProfile(userEmail, ds);
    if (profile != null) { 
      response.getWriter().append("Already registered?" + userEmail);
      System.out.println("Already registered?" + installId + " " + userEmail);
      return;
    }
    
    Entity user = ProfileServlet.retrieveUser(installId, ds);
    if (user == null) {
      response.getWriter().append("No such user: " + installId);
      return;       
    }
    
    user.setProperty(ProfileServlet.PROFILE_ID, userEmail);
    profile = ProfileServlet.createOrGetUserProfile(user, true);
    profile.setProperty(ProfileServlet.USER_NAME, userName);
    profile.setProperty(ProfileServlet.USER_ID, userEmail);
    
    Entity user1 = ProfileServlet.createOrGetUser(userEmail, ds, true);
    user1.setPropertiesFrom(user);
    ds.put(user1);
    ds.put(user);
    response.getWriter().append("<div style='background-color: black; width:64'>" + 
        "<img src='/userimage?userid=" + userEmail +"&png=pnguser'>" +
        "</div>");
    response.getWriter().append("Registration Completed");
  }

}

