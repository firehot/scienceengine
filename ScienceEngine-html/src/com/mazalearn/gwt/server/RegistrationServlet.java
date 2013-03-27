package com.mazalearn.gwt.server;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;

@SuppressWarnings("serial")
public class RegistrationServlet extends HttpServlet {

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String userEmail = request.getParameter("e");
    String installId = request.getParameter("i");
    String hash = request.getParameter("h");
    System.out.println("Register User: " + userEmail + " id: " + installId);

    if (!RegistrationEmailServlet.getHash(installId, userEmail).equals(hash)) {
      response.getWriter().append("Invalid registration info for: " + installId + " " + userEmail);
      return;
    }
    
    String pin = request.getParameter(ProfileServlet.PIN);
    if (pin != null) {
      pin = pin.toLowerCase();
    }
    
    if (!installId.substring(installId.length() - 4).equals(pin)) {
      response.getWriter().append("Invalid PIN");
      return;
    }
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity user = ProfileServlet.retrieveUser(userEmail, ds);
    if (user != null) { 
      response.getWriter().append("Already registered?" + installId + " " + userEmail);
      return;
    }
    
    user = ProfileServlet.retrieveUser(installId, ds);
    if (user == null) {
      response.getWriter().append("No such user: " + installId);
      return;       
    }
    
    EmbeddedEntity profile = ProfileServlet.createOrGetUserProfile(user);
    
    profile.setProperty(ProfileServlet.USER_NAME, request.getParameter(ProfileServlet.USER_NAME));
    profile.setProperty(ProfileServlet.USER_ID, userEmail);
    profile.setProperty(ProfileServlet.SEX, request.getParameter(ProfileServlet.SEX));
    profile.setProperty(ProfileServlet.GRADE, request.getParameter(ProfileServlet.GRADE));
    profile.setProperty(ProfileServlet.SCHOOL, request.getParameter(ProfileServlet.SCHOOL));
    profile.setProperty(ProfileServlet.CITY, request.getParameter(ProfileServlet.CITY));
    profile.setProperty(ProfileServlet.COMMENTS, request.getParameter(ProfileServlet.COMMENTS));
    Date date = new Date();
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    profile.setProperty(ProfileServlet.REGN_DATE, dateFormat.format(date));
    ds.put(user);

    Entity user1 = ProfileServlet.createOrGetUser(userEmail, ds);
    user1.setPropertiesFrom(user);
    ds.put(user1);
    response.getWriter().append("Registered: " + userEmail);
  }

}

