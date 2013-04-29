package com.mazalearn.gwt.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.mazalearn.scienceengine.app.services.ProfileData;
import com.mazalearn.scienceengine.app.services.ProfileData.ServerProps;

@SuppressWarnings("serial")
public class RegistrationServlet extends HttpServlet {

  public static final long EXPIRY_TIME_MS = 7 * 24 * 3600 * 1000; // 7 days
  private ProfileUtil profileUtil = new ProfileUtil();

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String installId = request.getParameter(ProfileData.INSTALL_ID).toLowerCase();
    // userEmail may be present, if installation registration is already done
    String userEmail = request.getParameter(ProfileData.USER_EMAIL).toLowerCase();
    System.out.println("Register User: " + installId + " " + userEmail);

    if (userEmail == null || !userEmail.contains("@")) {
      response.getWriter().append("Improper email. Cannot register");
      return;
    }
    String userName = request.getParameter(ProfileData.USER_NAME);
    if (userName == null || userName.length() < 2) {
      response.getWriter().append("Improper name. Cannot register");
      return;
    }

    // Case 1: Installation is already registered and user is being registered => installation user may not exist and user exists but not registered
    // Case 2: Both installation and user are being registered => installation user exists and user exists but not registered

    // Retrieve user using userEmail and if not present, retrieve using installId
    Entity user = profileUtil.retrieveUser(userEmail);
    if (user == null) { // Case 1 - check
      user = profileUtil.retrieveUser(installId);
      if (user == null || !user.getKey().getName().equals(userEmail)) {
        response.getWriter().append("Not properly synced to server? Could not find user");
        return;
      }
    } else {
      // If user is already registered, no need to register again.
      EmbeddedEntity profile = ProfileUtil.createOrGetUserProfile(user, false);
      ServerProps s = new JsonEntityUtil().getFromJsonTextProperty(profile, ProfileData.SERVER_PROPS, ServerProps.class);
      if (s != null && s.isRegistered) {
        response.getWriter().append("<html><body><br><br>");
        response.getWriter().append("Registration for user has already been done");
        return;
      }
    }

    // Get profile of user and save optional registration info
    EmbeddedEntity profile = ProfileUtil.createOrGetUserProfile(user, true);
    
    String sex = request.getParameter(ProfileData.SEX);
    String grade = request.getParameter(ProfileData.GRADE);
    String school = request.getParameter(ProfileData.SCHOOL);
    String city = request.getParameter(ProfileData.CITY);
    String comments = request.getParameter(ProfileData.COMMENTS);
    profileUtil.saveOptionalRegistrationInfo(user, profile, sex, grade, school,
        city, comments);

    EmailUtil.sendConfirmationEmail(userEmail, userName, installId);
    response.getWriter().append("<html><body><br><br>");
    response.getWriter().append("Registration in progress: " + userEmail);
    response.getWriter().append("<br><br>Email has been sent. <br>Please click on URL in email to complete registration.</body></html>");
  }
}

