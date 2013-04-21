package com.mazalearn.gwt.server;

import java.io.BufferedOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Text;
import com.mazalearn.scienceengine.app.services.ProfileData;

@SuppressWarnings("serial")
public class UserImageServlet extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    System.out.println("UserImage servlet - Received get: " + request.getContentLength());
    String userId = request.getParameter(ProfileData.USER_ID);
    String img = request.getParameter(ProfileData.PNG);
    System.out.println("UserId: " + userId);
    response.setHeader("ContentType", "image/png");
    BufferedOutputStream dis = new BufferedOutputStream(response.getOutputStream());
    byte[] userImage = getUserImage(userId, img);
    if (userImage != null) {
      dis.write(userImage);
    }
    dis.close();
  }

  public byte[] getUserImage(String userEmail, String img) throws IllegalStateException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    EmbeddedEntity profileEntity = ProfileServlet.retrieveUserProfile(userEmail, ds);
    if (profileEntity != null) {
      Text coachPngBase64 = (Text) profileEntity.getProperty(img);
      if (coachPngBase64 != null) {
        return Base64.decode(coachPngBase64.getValue());
      }
    }
    return null;
  }

}
