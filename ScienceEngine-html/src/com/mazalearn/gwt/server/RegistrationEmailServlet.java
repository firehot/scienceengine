package com.mazalearn.gwt.server;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.PropertyContainer;
import com.mazalearn.scienceengine.app.services.InstallData;
import com.mazalearn.scienceengine.app.services.ProfileData;
import com.mazalearn.scienceengine.app.services.ProfileData.ServerProps;
import com.mazalearn.scienceengine.app.utils.Crypter;

/**
 * Invoked when the user clicks on received registration email to confirm.
 * The email has an expiry time and a salted verification code
 * to discourage replay and substitution attacks.
 * @author sridhar
 *
 */
@SuppressWarnings("serial")
public class RegistrationEmailServlet extends HttpServlet {

  static final String USER_EMAIL = "useremail";
  static final String INSTALL_ID = "installid";
  private ProfileUtil profileUtil = new ProfileUtil();
  private JsonEntityUtil jsonEntityUtil = new JsonEntityUtil();

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    System.out.println("Register - Received get: " + request.getContentLength());
    String userEmail = request.getParameter("e").toLowerCase();
    String installId = request.getParameter("i");
    String userName = request.getParameter("n");
    long timeEmailSent = Long.parseLong(request.getParameter("t"));
    String hash = request.getParameter("h");
    System.out.println("User: " + userEmail + " id: " + installId);

    response.setContentType("text/html");

    if (System.currentTimeMillis() - timeEmailSent >= RegistrationServlet.EXPIRY_TIME_MS) {
      response.getWriter().append("Registration email has expired");
      return;
    }
    
    String hash1 = URLEncoder.encode(hash, "UTF-8");
    String hash2 = Crypter.saltedSha1Hash(installId + userEmail + userName + timeEmailSent, installId);
    if (!hash2.equals(hash1)) {
      response.getWriter().append("Invalid registration info for: " + userEmail + "<br>");
      System.out.println("Invalid registration info for: " + installId + " " + userEmail + " " + userName + " " + timeEmailSent);
      System.out.println("Hash1=<" + hash1 + "> Hash2=<" + hash2);
      return;
    }
    
    String installRegistrationResponse = registerInstallation(installId, userEmail);

    PropertyContainer user = RegistrationServlet.validateRegistrationInfo(profileUtil, response, installId,
        userEmail);
    if (user == null) return;
    EmbeddedEntity newUserProfile = ProfileUtil.createOrGetUserProfile(user, true);
    
    profileUtil.confirmRegistrationInfo(userEmail, installId, userName, 
        newUserProfile, user);
    
    response.getWriter().append("<div style='background-color: black; width:64'>" + 
        "<img src='/userimage?userid=" + userEmail +"&png=pnguser'>" +
        "</div>");
    response.getWriter().append(installRegistrationResponse);
    response.getWriter().append("Registration Completed: Thank you, " + userEmail);
  }

  private String registerInstallation(String installId, String userEmail) {
    PropertyContainer installProfile = profileUtil.createOrGetInstall(installId, false);
    InstallData installData = jsonEntityUtil.getFromJsonTextProperty(installProfile, InstallData.INSTALL_DATA, InstallData.class);
    if (installData == null) {
      throw new UnsupportedOperationException("Installation not found - sync not done?");
    }
    if (installData.registeredUserId != null) {
      return "This installation is registered to: " + installData.registeredUserId + "<br>";
    } else if (userEmail != null) {
      installData.registeredUserId = userEmail;
      jsonEntityUtil.setAsJsonTextProperty(installProfile, InstallData.INSTALL_DATA, installData);
      profileUtil.saveEntity(installProfile);
      return "Installation not yet registered - registering to user: " + userEmail + "<br>";
    } else {
      throw new UnsupportedOperationException("Installation not found - sync not done?");
    }
  }

  // For testing only
  void setProfileUtil(ProfileUtil dummyProfileUtil) {
    profileUtil = dummyProfileUtil;
  }
}

