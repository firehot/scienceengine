package com.mazalearn.gwt.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import com.google.gson.Gson;

@SuppressWarnings("serial")
public class ProfileServlet extends HttpServlet {

  static final String USER_EMAIL = "useremail";

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    System.out.println("Received post: " + request.getContentLength());
    response.getWriter().append("Post received");
    String userEmail = request.getHeader(USER_EMAIL);
    System.out.println("User: " + userEmail);
    BufferedInputStream bis = new BufferedInputStream(request.getInputStream());
    byte[] profileBytes = new byte[request.getContentLength()];
    bis.read(profileBytes);
    saveUserProfile(userEmail, profileBytes);
    bis.close();
  }
  
  static class Profile {
    Map<String, String> properties;
    Map<String, Map<String, Float>> domains;
  }

  public void saveUserProfile(String userEmail, byte[] profileBytes) 
      throws IllegalStateException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Key key = KeyFactory.createKey(User.class.getSimpleName(), userEmail);
    Entity user;
    try {
      user = ds.get(key);
    } catch (EntityNotFoundException e) {
      user = new Entity(User.class.getSimpleName(), userEmail);
      ds.put(user);
    }
    String profileStringBase64 = new String(profileBytes);
    String profileStringJson = new String(Base64.decode(profileStringBase64));
    
    // Trim at end where 0 chars are present.
    int count = profileStringJson.length();
    while (profileStringJson.charAt(--count) == 0);
    Gson gson = new Gson();
    Profile profile = gson.fromJson(profileStringJson.substring(0, count+1), Profile.class);
    
    EmbeddedEntity profileEntity = (EmbeddedEntity) user.getProperty("Profile");
    if (profileEntity == null) {
      profileEntity = new EmbeddedEntity();
      user.setProperty("Profile", profileEntity);
    }
    for (Map.Entry<String, String> entry: profile.properties.entrySet()) {
      profileEntity.setProperty(entry.getKey(), entry.getValue());
    }
    for (Map.Entry<String, Map<String, Float>> domainStats: profile.domains.entrySet()) {
      String jsonStats = gson.toJson(domainStats.getValue());
      profileEntity.setProperty(domainStats.getKey(), new Text(jsonStats));
    }
    ds.put(user);
  }
  

}
