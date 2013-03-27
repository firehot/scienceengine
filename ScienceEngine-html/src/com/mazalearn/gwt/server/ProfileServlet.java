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

  public static final String PROFILE = "Profile";
  public static final String USER_ID = "userid"; // owner
  public static final String DRAWING_PNG = "DrawingPng";
  public static final String USER_NAME = "username";
  public static final String CURRENT = "current";
  public static final String COLOR = "color";
  public static final String USER_EMAIL = "useremail"; // param
  public static final String SEX = "sex";  // owner
  public static final String GRADE = "grade"; // owner
  public static final String SCHOOL = "school"; // owner
  public static final String CITY = "city";     // owner
  public static final String COMMENTS = "comments"; // owner
  public static final String REGN_DATE = "regndate"; // owner
  public static final String INSTALL_ID = "installid";
  public static final String PIN = "pin"; // readonly

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    System.out.println("Received post: " + request.getContentLength());
    String userId = request.getHeader(USER_ID);
    System.out.println("UserId: " + userId);
    BufferedInputStream bis = new BufferedInputStream(request.getInputStream());
    byte[] profileBytes = new byte[request.getContentLength()];
    bis.read(profileBytes);
    saveUserProfile(userId, profileBytes);
    bis.close();
    writeProfileResponse(response, userId);
  }
  
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String userId = request.getParameter(USER_ID);
    System.out.println("Received get: " + userId);
    writeProfileResponse(response, userId);
  }

  private void writeProfileResponse(HttpServletResponse response, String userId)
      throws IOException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    response.getWriter().append(getUserProfileAsBase64(userId, ds));
    response.getWriter().close();
  }
  
  static class Profile {
    Map<String, String> properties;
    Map<String, Map<String, float[]>> topics;
  }

  public void saveUserProfile(String userId, byte[] profileBytes) 
      throws IllegalStateException {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity user = createOrGetUser(userId, ds);
    String profileStringBase64 = new String(profileBytes);
    String profileStringJson = new String(Base64.decode(profileStringBase64));
    
    // Trim at end where 0 chars are present.
    int count = profileStringJson.length();
    while (profileStringJson.charAt(--count) == 0);
    Gson gson = new Gson();
    System.out.println(profileStringJson.substring(0, count + 1));
    Profile profile = gson.fromJson(profileStringJson.substring(0, count+1), Profile.class);
    
    EmbeddedEntity profileEntity = createOrGetUserProfile(user);
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
    String userEmail = (String) profileEntity.getProperty(USER_EMAIL);
    if (userId.equals(userEmail)) {
      // Delete installation id based user, if any
      user = retrieveUser((String) profileEntity.getProperty(INSTALL_ID), ds);
      if (user != null) {
        ds.delete(user.getKey());
      }
    }
  }

  public static EmbeddedEntity createOrGetUserProfile(Entity user) {
    EmbeddedEntity profileEntity = (EmbeddedEntity) user.getProperty(PROFILE);
    if (profileEntity == null) {
      profileEntity = new EmbeddedEntity();
      user.setProperty(PROFILE, profileEntity);
    }
    return profileEntity;
  }

  public static Entity createOrGetUser(String userId, DatastoreService ds) {
    Key key = KeyFactory.createKey(User.class.getSimpleName(), userId.toLowerCase());
    Entity user;
    try {
      user = ds.get(key);
    } catch (EntityNotFoundException e) {
      user = new Entity(User.class.getSimpleName(), userId.toLowerCase());
      ds.put(user);
    }
    return user;
  }
  
  public String getUserProfileAsBase64(String userId, DatastoreService ds) 
      throws IllegalStateException {
    EmbeddedEntity profileEntity = retrieveUserProfile(userId, ds);
    if (profileEntity == null) {
      System.out.println("No user profile: " + userId);
      return "";
    }
    
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

  public static EmbeddedEntity retrieveUserProfile(String userId, DatastoreService ds) {
    Entity user = retrieveUser(userId, ds);
    if (user == null) return null;
    EmbeddedEntity profileEntity = (EmbeddedEntity) user.getProperty(PROFILE);
    return profileEntity;
  }

  public static Entity retrieveUser(String userId, DatastoreService ds) {
    if (userId == null || userId.length() == 0) return null;
    Key key = KeyFactory.createKey(User.class.getSimpleName(), userId.toLowerCase());
    Entity user;
    try {
      user = ds.get(key);
    } catch (EntityNotFoundException e) {
      System.out.println("No such user: " + userId);
      return null;
    }
    return user;
  }
}
