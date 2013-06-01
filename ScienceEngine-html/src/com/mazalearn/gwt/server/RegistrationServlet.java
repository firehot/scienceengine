package com.mazalearn.gwt.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PropertyContainer;
import com.mazalearn.scienceengine.app.services.InstallData;
import com.mazalearn.scienceengine.app.services.ProfileData;

@SuppressWarnings("serial")
public class RegistrationServlet extends HttpServlet {

  public static final long EXPIRY_TIME_MS = 7 * 24 * 3600 * 1000; // 7 days
  private ProfileUtil profileUtil = new ProfileUtil();
  private EmailUtil emailUtil = new EmailUtil();

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String installId = request.getParameter(ProfileData.INSTALL_ID);
    // userEmail may be present, if installation registration is already done
    String userEmail = request.getParameter(ProfileData.USER_EMAIL);
    System.out.println("Register User: " + installId + " " + userEmail);

    if (userEmail == null || !userEmail.contains("@")) {
      response.getWriter().append("Improper email. Cannot register");
      return;
    }
    userEmail = userEmail.toLowerCase();
    String userName = request.getParameter(ProfileData.USER_NAME);
    if (userName == null || userName.length() < 2) {
      response.getWriter().append("Improper name. Cannot register");
      return;
    }

    PropertyContainer user = validateRegistrationInfo(profileUtil, response, installId,
        userEmail);
    if (user == null) return;
    
    // Get profile of user and save optional registration info
    EmbeddedEntity profile = ProfileUtil.createOrGetUserProfile(user, true);
    
    String sex = request.getParameter(ProfileData.SEX);
    String grade = request.getParameter(ProfileData.GRADE);
    String school = request.getParameter(ProfileData.SCHOOL);
    String city = request.getParameter(ProfileData.CITY);
    String comments = request.getParameter(ProfileData.COMMENTS);
    profileUtil.saveOptionalRegistrationInfo(user, profile, sex, grade, school,
        city, comments);

    emailUtil.sendConfirmationEmail(userEmail, userName, installId);
    response.getWriter().append("<html><body><br><br>");
    response.getWriter().append("Registration in progress: " + userEmail);
    response.getWriter().append("<br><br>Email has been sent. <br>Please click on URL in email to complete registration.</body></html>");
  }

  static PropertyContainer validateRegistrationInfo(ProfileUtil profileUtil,
      HttpServletResponse response, String installId, String userEmail)
      throws IOException {
    // Case 1: Installation registered and new user not registered => 
    //         Allowed only for corporate
    //         installId user exists and user does not exist
    // Case 2: Installation not registered and user not registered => 
    //         installId user exists and user does not exist

    // Retrieve installation profile
    InstallData installData = profileUtil.retrieveInstallProfile(installId);
    if (installData == null) {
      response.getWriter().append("Not properly synced to server? Could not find installation");
      return null;      
    }
    if (installData.registeredUserId != null && // already registered 
        !installData.registeredUserId.equals(userEmail) && // not this user
        installData.enterpriseId == null) { // not corporate
      response.getWriter().append("Installation already registered to: " + userEmail + 
          "\nOnly one user can be registered for an installation");
      return null;      
    }
    
    // Retrieve user using userEmail and if not present, retrieve using installId
    PropertyContainer user = profileUtil.retrieveUser(userEmail);
    if (user == null) {
      user = profileUtil.retrieveUser(installId);
      String name = null;
      if (user == null) { name = ""; }
      else if (user instanceof Entity) { name = ((Entity)user).getKey().getName(); }
      else if (user instanceof EmbeddedEntity) { name =  (String) ((EmbeddedEntity)user).getProperty("KEY"); }
      if (user == null || (!name.equals(userEmail) && !name.equals(installId))) {
        response.getWriter().append("Not properly synced to server? Could not find user");
        return null;
      }
    }
    return user;
  }

  // for testing
  public void setProfileUtil(ProfileUtil profileUtil) {
    this.profileUtil = profileUtil;
  }

  // for testing
  public void setEmailUtil(EmailUtil emailUtil) {
    this.emailUtil = emailUtil;
  }
}

