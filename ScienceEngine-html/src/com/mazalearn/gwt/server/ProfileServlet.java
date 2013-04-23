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
    String oldUserId = profileUtil.saveUserProfile(userId, clientProfile);
    writeProfileResponse(response, userId, clientProfile);
    // Delete old user, if any
    if (oldUserId != null) {
      profileUtil.deleteOldUser(userId, oldUserId);
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String userId = request.getParameter(ProfileData.USER_ID);
    System.out.println("Received get: " + userId);
    writeProfileResponse(response, userId, null);
  }

  private void writeProfileResponse(HttpServletResponse response, String userId, ProfileData clientProfile)
      throws IOException {
    String responseStr = profileUtil.getUserSyncProfileAsBase64(userId, clientProfile);
    if (responseStr.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    } else {
      response.getWriter().append(responseStr);
    }
    response.getWriter().close();
  }

}
