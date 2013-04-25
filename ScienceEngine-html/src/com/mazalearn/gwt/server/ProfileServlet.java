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
  private ProfileUtil profileUtil;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    System.out.println("Received post: " + request.getContentLength());
    String userId = request.getHeader(ProfileData.USER_ID);
    System.out.println("UserId: " + userId);
    
    BufferedInputStream bis = new BufferedInputStream(request.getInputStream());
    byte[] profileBytes = new byte[request.getContentLength()];
    bis.read(profileBytes);
    bis.close();
    
    profileUtil = new ProfileUtil();
    ProfileData clientProfile = profileUtil.profileFromBase64(profileBytes);
    String syncProfileBase64 = profileUtil.saveUserProfile(userId, clientProfile);
    writeProfileResponse(response, syncProfileBase64);
    // Delete old user, if any
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String userId = request.getParameter(ProfileData.USER_ID);
    System.out.println("Received get: " + userId);
//    writeProfileResponse(response, userId, null);
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

}
