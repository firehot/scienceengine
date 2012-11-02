package com.mazalearn.gwt.server;


import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * Filter to check authorization for user before proceeding
 * 
 */
public class UserFilter implements Filter {

  /**
   * Check whether the user is logged in. Return the current User information or
   * throw an exception.
   * 
   * @return String
   * @throws LoginService.NotLoggedInException
   */

  private User checkUserLoggedIn() throws IllegalStateException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn())
      throw new IllegalStateException("Not logged in");
    return userService.getCurrentUser();
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
      throws ServletException, IOException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    User user = null;
    try {
      user = checkUserLoggedIn();
    } catch (IllegalStateException e) {
      throw new ServletException("User not logged in");
    }
    if (UserPermissionServlet.checkUserPermitted(user.getEmail())) {
      String requestURL = httpRequest.getRequestURI() + "?" + httpRequest.getQueryString();
      httpResponse.sendRedirect("/#" + requestURL);
    }
    response.getWriter().append("Sorry, please request permission to view demo");
  }

  @Override
  public void destroy() {
  }
  
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }
}