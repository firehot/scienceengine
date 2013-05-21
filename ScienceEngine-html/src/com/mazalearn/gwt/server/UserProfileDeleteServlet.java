package com.mazalearn.gwt.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mazalearn.scienceengine.app.services.ProfileData;

@SuppressWarnings("serial")
public class UserProfileDeleteServlet extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    System.out.println("Received get: " + request.getContentLength());
    String userEmail = request.getParameter(ProfileData.USER_ID);
    System.out.println("User: " + userEmail);
    ProfileUtil profileUtil = new ProfileUtil();
    profileUtil.resetUserProfile(userEmail);
    response.getWriter().append("User Profile deleted");
  }
}
