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

  public static final String PROFILE = "Profile";
  public static final String USER_ID = "userid"; // owner
  static final String PNG = "png";
  public static final String COACH_PNG = PNG + "coach";
  public static final String USER_PNG = PNG + "user";
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
  // The profileId in a user entity forwards to the right profile.
  public static final String PROFILE_ID = "profileid"; // registration is the owner

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    System.out.println("Received post: " + request.getContentLength());
    String userId = request.getHeader(USER_ID);
    System.out.println("UserId: " + userId);
    BufferedInputStream bis = new BufferedInputStream(request.getInputStream());
    byte[] profileBytes = new byte[request.getContentLength()];
    bis.read(profileBytes);
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    saveUserProfile(userId, profileBytes, ds);
    bis.close();
    writeProfileResponse(response, userId, ds);
    if (userId.indexOf("@") != -1) {
      // TODO: this is an expensive way of doing - double retrieve and delete.
      // Delete installation id based user, if any
      EmbeddedEntity profileEntity = retrieveUserProfile(userId, ds);
      String installId = (String) profileEntity.getProperty(INSTALL_ID);
      Key key = KeyFactory.createKey(User.class.getSimpleName(), installId.toLowerCase());
      ds.delete(key);
      System.out.println("Deleted: " + installId);
    }
  }
  
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String userId = request.getParameter(USER_ID);
    System.out.println("Received get: " + userId);
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    writeProfileResponse(response, userId, ds);
  }

  private void writeProfileResponse(HttpServletResponse response, String userId, DatastoreService ds)
      throws IOException {
    response.getWriter().append(getUserProfileAsBase64(userId, ds));
    response.getWriter().close();
  }
  
  static class Profile {
    Map<String, String> properties;
    Map<String, Map<String, float[]>> topics;
  }

  public void saveUserProfile(String userId, byte[] profileBytes, DatastoreService ds) 
      throws IllegalStateException {
    Entity user = createOrGetUser(userId, ds, true);
    String profileStringBase64 = new String(profileBytes);
    String profileStringJson = new String(Base64.decode(profileStringBase64));
    
    // Trim at end where 0 chars are present.
    int count = profileStringJson.length();
    while (profileStringJson.charAt(--count) == 0);
    Gson gson = new Gson();
    System.out.println(profileStringJson.substring(0, count + 1));
    Profile profile = gson.fromJson(profileStringJson.substring(0, count+1), Profile.class);
    
    EmbeddedEntity profileEntity = createOrGetUserProfile(user, true);
    for (Map.Entry<String, String> entry: profile.properties.entrySet()) {
      if (entry.getKey().startsWith(PNG)) { // Too large and base64encoded - store as TEXT
        if (entry.getValue() != null) {
          profileEntity.setProperty(entry.getKey(), new Text(entry.getValue()));
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

  public static EmbeddedEntity createOrGetUserProfile(Entity user, boolean create) {
    if (user == null) return null;
    EmbeddedEntity profileEntity = (EmbeddedEntity) user.getProperty(PROFILE);
    if (create && profileEntity == null) {
      profileEntity = new EmbeddedEntity();
      user.setProperty(PROFILE, profileEntity);
    }
    return profileEntity;
  }

  public static Entity createOrGetUser(String userId, DatastoreService ds, boolean create) {
    if (userId == null || userId.length() == 0) return null;
    Key key = KeyFactory.createKey(User.class.getSimpleName(), userId.toLowerCase());
    Entity user = null, user1 = null;
    try {
      user = ds.get(key);
      String profileId = (String) user.getProperty(PROFILE_ID);
      if (profileId != null && profileId.length() > 0) {
        key = KeyFactory.createKey(User.class.getSimpleName(), profileId);
        user1 = ds.get(key);
        user = user1;
      }
    } catch (EntityNotFoundException e) {
      if (create && user == null) {
        user = new Entity(User.class.getSimpleName(), userId.toLowerCase());
        ds.put(user);
      }
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
        if (property.getKey().startsWith(PNG)) {
          properties.append(propDelimiter + property.getKey() + ":\"" + s + "\"");
          propDelimiter = ",";
        } else if (!"null".equals(s)) {
          topics.append(topicDelimiter + property.getKey() + ":" + s);
          topicDelimiter = ",";
        }
      } else {
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
    Entity user = createOrGetUser(userId, ds, false);
    return createOrGetUserProfile(user, false);
  }

  public static Entity retrieveUser(String userId, DatastoreService ds) {
    return createOrGetUser(userId, ds, false);
  }
}
