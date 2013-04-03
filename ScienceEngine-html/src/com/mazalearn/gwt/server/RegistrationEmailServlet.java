package com.mazalearn.gwt.server;

import java.io.IOException;
import java.net.URLEncoder;

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
    
    String hash1 = URLEncoder.encode(hash, "UTF-8");
    String hash2 = RegistrationServlet.getHash(installId, userEmail, userName, timeEmailSent);
    if (!hash2.equals(hash1)) {
      response.getWriter().append("Invalid registration info for: " + userEmail);
      System.out.println("Invalid registration info for: " + installId + " " + userEmail + " " + userName + " " + timeEmailSent);
      System.out.println("Hash1=<" + hash1 + "> Hash2=<" + hash2);
      return;
    }
    
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    // User may exist but should not have profile
    EmbeddedEntity newUserProfile = ProfileServlet.retrieveUserProfile(userEmail, ds);
    if (newUserProfile != null) { 
      response.getWriter().append("Already registered. " + userEmail);
      System.out.println("Already registered to: " + newUserProfile.getProperty(ProfileServlet.INSTALL_ID) + " " + userEmail);
      return;
    }
    
    Entity oldUser = ProfileServlet.retrieveUser(installId, ds);
    if (oldUser == null) {
      response.getWriter().append("No such user: " + installId);
      return;       
    }
    
    EmbeddedEntity oldUserProfile = ProfileServlet.createOrGetUserProfile(oldUser, true);
    oldUserProfile.setProperty(ProfileServlet.USER_NAME, userName);
    oldUserProfile.setProperty(ProfileServlet.USER_ID, userEmail);
    oldUserProfile.setProperty(ProfileServlet.PROFILE, newUserProfile);
    
    Entity newUser = ProfileServlet.createOrGetUser(userEmail, ds, true);
    newUser.setPropertiesFrom(oldUser);

    newUser.setProperty(ProfileServlet.OLD_USER_ID, installId);
    ds.put(newUser);
    oldUser.setProperty(ProfileServlet.NEW_USER_ID, userEmail);
    ds.put(oldUser);
    
    response.getWriter().append("<div style='background-color: black; width:64'>" + 
        "<img src='/userimage?userid=" + userEmail +"&png=pnguser'>" +
        "</div>");
    response.getWriter().append("Registration Completed: Thank you, " + userEmail);
  }

}

