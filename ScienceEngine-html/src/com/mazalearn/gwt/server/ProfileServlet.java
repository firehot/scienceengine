package com.mazalearn.gwt.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Blob;
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

  static final String PROFILE = "Profile";
  static final String USER_EMAIL = "useremail";
  static final String DRAWING_PNG = "DrawingPng";

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    System.out.println("Received post: " + request.getContentLength());
    String userEmail = request.getHeader(USER_EMAIL);
    System.out.println("User: " + userEmail);
    BufferedInputStream bis = new BufferedInputStream(request.getInputStream());
    byte[] profileBytes = new byte[request.getContentLength()];
    bis.read(profileBytes);
    saveUserProfile(userEmail, profileBytes);
    bis.close();
  }
  
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String userEmail = request.getParameter(USER_EMAIL);
    System.out.println("Received get: " + userEmail);
    response.getWriter().append(getUserProfile(userEmail));
    response.getWriter().close();
  }
  
  static class Profile {
    Map<String, String> properties;
    Map<String, Map<String, float[]>> topics;
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
    System.out.println(profileStringJson.substring(0, count + 1));
    Profile profile = gson.fromJson(profileStringJson.substring(0, count+1), Profile.class);
    
    EmbeddedEntity profileEntity = (EmbeddedEntity) user.getProperty(PROFILE);
    if (profileEntity == null) {
      profileEntity = new EmbeddedEntity();
      user.setProperty(PROFILE, profileEntity);
    }
    for (Map.Entry<String, String> entry: profile.properties.entrySet()) {
      if (entry.getKey().equals(DRAWING_PNG)) {
        if (entry.getValue() != null) {
          byte[] bytes = Base64.decode(entry.getValue());
          profileEntity.setProperty(entry.getKey(), new Blob(bytes));
        }
      } else {
        profileEntity.setProperty(entry.getKey(), entry.getValue());
      }
    }
    for (Map.Entry<String, Map<String, float[]>> topicStats: profile.topics.entrySet()) {
      String jsonStats = gson.toJson(topicStats.getValue());
      profileEntity.setProperty(topicStats.getKey(), new Text(jsonStats));
    }
    ds.put(user);
  }
  
  public String getUserProfile(String userEmail) 
      throws IllegalStateException {
    EmbeddedEntity profileEntity = retrieveUserProfile(userEmail);
    if (profileEntity == null) return "";
    
    System.out.println(profileEntity);
    StringBuilder properties = new StringBuilder("{");
    StringBuilder topics = new StringBuilder("{");
    String propDelimiter = "", topicDelimiter = "";
    for (Map.Entry<String, Object> property: profileEntity.getProperties().entrySet()) {
      Object value = property.getValue();
      if (value instanceof Text) {
        String s = ((Text) value).getValue();
        if (!"null".equals(s)) {
          topics.append(topicDelimiter + property.getKey() + ":" + s);
          topicDelimiter = ",";
        }
      } else if (!property.getKey().equals(DRAWING_PNG)){
        properties.append(propDelimiter + property.getKey() + ":\"" + value + "\"");
        propDelimiter = ",";
      }
    }
    properties.append("}");
    topics.append("}");
    String json = "{ properties:" + properties + ",topics:" + topics + "}";
    System.out.println(json);
    String profileStringBase64 = Base64.encode(json);
    return profileStringBase64;
  }

  public static EmbeddedEntity retrieveUserProfile(String userEmail) {
    Entity user = retrieveUser(userEmail);
    if (user == null) return null;
    EmbeddedEntity profileEntity = (EmbeddedEntity) user.getProperty(PROFILE);
    return profileEntity;
  }

  public static Entity retrieveUser(String userEmail) {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Key key = KeyFactory.createKey(User.class.getSimpleName(), userEmail);
    Entity user;
    try {
      user = ds.get(key);
    } catch (EntityNotFoundException e) {
      return null;
    }
    return user;
  }
}
