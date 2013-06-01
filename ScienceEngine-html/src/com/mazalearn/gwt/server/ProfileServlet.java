package com.mazalearn.gwt.server;

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mazalearn.scienceengine.app.services.ProfileData;

@SuppressWarnings("serial")
public class ProfileServlet extends HttpServlet {

  public static final String PROFILE = "Profile";
  // The profileId in a user entity forwards to the right profile.
  public static final String NEW_USER_ID = "newuserid"; // email verification is the owner
  public static final String OLD_USER_ID = "olduserid"; // email verification is the owner
  private ProfileUtil profileUtil = new ProfileUtil();

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    System.out.println("Received post: " + request.getContentLength());
    String userId = request.getHeader(ProfileData.USER_ID);
    System.out.println("UserId: " + userId);
    
    BufferedInputStream bis = new BufferedInputStream(request.getInputStream());
    byte[] profileBytes = new byte[request.getContentLength()];
    bis.read(profileBytes);
    bis.close();
    
    ProfileData clientProfile = profileUtil.profileFromBase64(profileBytes);
    String syncProfileBase64 = profileUtil.saveUserProfile(userId, clientProfile);
    writeProfileResponse(response, syncProfileBase64);
  }

  private void writeProfileResponse(HttpServletResponse response, String syncProfileBase64)
      throws IOException {
    if (syncProfileBase64.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    } else {
      response.getWriter().append(syncProfileBase64);
    }
    response.getWriter().close();
  }

  // for testing
  void setProfileUtil(ProfileUtil profileUtil) {
    this.profileUtil = profileUtil;
  }

}
