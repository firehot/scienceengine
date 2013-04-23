package com.mazalearn.gwt.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.mazalearn.scienceengine.app.services.ProfileData;

@SuppressWarnings("serial")
public class RegistrationServlet extends HttpServlet {

  public static final long EXPIRY_TIME_MS = 7 * 24 * 3600 * 1000; // 7 days

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String userId = request.getParameter(ProfileData.INSTALL_ID).toLowerCase();
    System.out.println("Register User: " + userId);

    ProfileUtil profileUtil = new ProfileUtil();
    Entity user = profileUtil.retrieveUser(userId);
    if (user == null) { 
      response.getWriter().append("Already registered? Could not find installation");
      return;
    }

    EmbeddedEntity profile = ProfileUtil.createOrGetUserProfile(user, true);
    
    String userEmail = request.getParameter(ProfileData.USER_EMAIL).toLowerCase();
    if (userEmail == null || !userEmail.contains("@")) {
      response.getWriter().append("Improper email. Cannot register");
      return;
    }
    String userName = request.getParameter(ProfileData.USER_NAME);
    if (userName == null || userName.length() < 2) {
      response.getWriter().append("Improper name. Cannot register");
      return;
    }

    String sex = request.getParameter(ProfileData.SEX);
    String grade = request.getParameter(ProfileData.GRADE);
    String school = request.getParameter(ProfileData.SCHOOL);
    String city = request.getParameter(ProfileData.CITY);
    String comments = request.getParameter(ProfileData.COMMENTS);
    profileUtil.saveRegistrationInfo(user, profile, sex, grade, school,
        city, comments);

    EmailUtil.sendConfirmationEmail(userEmail, userName, userId);
    response.getWriter().append("<html><body><br><br>");
    response.getWriter().append("Registration in progress: " + userEmail);
    response.getWriter().append("<br><br>Email has been sent. <br>Please click on URL in email to complete registration.</body></html>");
  }
}

