package com.mazalearn.gwt.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class ScienceEngineServlet extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String requestURI = request.getRequestURI();
    System.out.println(requestURI);
    if (requestURI.equals("/demo")) {
      RequestDispatcher requestDispatcher = 
          request.getRequestDispatcher("/ncert-science-electromagnetism.pdf");
      requestDispatcher.forward(request, response);
      return;
    }
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn())
      throw new IllegalStateException("Not logged in");
    if (!UserPermissionServlet.checkUserPermitted(userService.getCurrentUser().getEmail())) {
      response.getWriter().append("Sorry, please request permission to view demo");
      return;
    }
    serveFile(response, "/WEB-INF/index.html", "text/html");
  }

  private void serveFile(HttpServletResponse response, String filename, String contentType)
      throws IOException {
    response.setContentType(contentType);
    InputStream inp = getServletContext().getResourceAsStream(filename);
    if (inp != null) {
      InputStreamReader isr = new InputStreamReader(inp);
      BufferedReader reader = new BufferedReader(isr);
      String text = "";
      while ((text = reader.readLine()) != null) {
        response.getWriter().append(text);
      }
    }
  }
}
