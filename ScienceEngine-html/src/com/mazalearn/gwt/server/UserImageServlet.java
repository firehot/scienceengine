package com.mazalearn.gwt.server;

import java.io.BufferedOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;

@SuppressWarnings("serial")
public class UserImageServlet extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    System.out.println("Received get: " + request.getContentLength());
    String userId = request.getParameter(ProfileServlet.USER_ID);
    System.out.println("UserId: " + userId);
    response.setHeader("ContentType", "image/png");
    BufferedOutputStream dis = new BufferedOutputStream(response.getOutputStream());
    Blob userImage = getUserImage(userId);
    if (userImage != null) {
      dis.write(userImage.getBytes());
    }
    dis.close();
  }

  public Blob getUserImage(String userEmail) throws IllegalStateException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    EmbeddedEntity profileEntity = ProfileServlet.retrieveUserProfile(userEmail, ds);
    if (profileEntity != null) {
      return (Blob) profileEntity.getProperty(ProfileServlet.DRAWING_PNG);
    }
    return null;
  }

}
