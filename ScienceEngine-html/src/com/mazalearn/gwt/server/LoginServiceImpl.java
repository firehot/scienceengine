package com.mazalearn.gwt.server;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service. Note all methods check to
 * see if the user is logged in before proceeding.
 * 
 */
@SuppressWarnings("serial")
public class LoginServiceImpl extends RemoteServiceServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    try {
      checkUserLoggedIn();
    } catch (IllegalStateException e) {
      e.printStackTrace();
      throw new ServletException("User not logged in");
    }
    String requestURL = request.getRequestURI() + "?" + request.getQueryString();
    response.sendRedirect("/#" + requestURL);
  }

  /**
   * Check whether the user is logged in. Return the current User information or
   * throw an exception.
   * 
   * @return String
   * @throws LoginService.NotLoggedInException
   */

  public User checkUserLoggedIn() throws IllegalStateException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn())
      throw new IllegalStateException("Not logged in");
    return userService.getCurrentUser();
  }
}
