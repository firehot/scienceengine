package com.mazalearn.gwt.server;

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
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
    
    profileUtil = new ProfileUtil();
    String oldUserId = profileUtil.saveUserProfile(userId, profileBytes);
    bis.close();
    writeProfileResponse(response, userId);
    // Delete old user, if any
    if (oldUserId != null) {
      DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
      Key key = KeyFactory.createKey(User.class.getSimpleName(), oldUserId.toLowerCase());
      ds.delete(key);
      System.out.println("Deleted: " + oldUserId);
      Entity newUser = profileUtil.retrieveUser(userId);
      newUser.removeProperty(OLD_USER_ID);
      ds.put(newUser);
    }
  }
  
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String userId = request.getParameter(ProfileData.USER_ID);
    System.out.println("Received get: " + userId);
    writeProfileResponse(response, userId);
  }

  private void writeProfileResponse(HttpServletResponse response, String userId)
      throws IOException {
    String responseStr = profileUtil.getUserSyncProfileAsBase64(userId);
    if (responseStr.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    } else {
      response.getWriter().append(responseStr);
    }
    response.getWriter().close();
  }

}
