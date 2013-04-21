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
import com.mazalearn.scienceengine.app.services.ProfileData;

@SuppressWarnings("serial")
public class ProfileServlet extends HttpServlet {

  public static final String PROFILE = "Profile";
  // The profileId in a user entity forwards to the right profile.
  public static final String NEW_USER_ID = "newuserid"; // email verification is the owner
  public static final String OLD_USER_ID = "olduserid"; // email verification is the owner

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    System.out.println("Received post: " + request.getContentLength());
    String userId = request.getHeader(ProfileData.USER_ID);
    System.out.println("UserId: " + userId);
    BufferedInputStream bis = new BufferedInputStream(request.getInputStream());
    byte[] profileBytes = new byte[request.getContentLength()];
    bis.read(profileBytes);
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    String oldUserId = saveUserProfile(userId, profileBytes, ds);
    bis.close();
    writeProfileResponse(response, userId, ds);
    // Delete old user, if any
    if (oldUserId != null) {
      Key key = KeyFactory.createKey(User.class.getSimpleName(), oldUserId.toLowerCase());
      ds.delete(key);
      System.out.println("Deleted: " + oldUserId);
      Entity newUser = retrieveUser(userId, ds);
      newUser.removeProperty(OLD_USER_ID);
      ds.put(newUser);
    }
  }
  
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String userId = request.getParameter(ProfileData.USER_ID);
    System.out.println("Received get: " + userId);
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    writeProfileResponse(response, userId, ds);
  }

  private void writeProfileResponse(HttpServletResponse response, String userId, DatastoreService ds)
      throws IOException {
    String responseStr = getUserProfileAsBase64(userId, ds);
    if (responseStr.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    } else {
      response.getWriter().append(responseStr);
    }
    response.getWriter().close();
  }
  
  private String saveUserProfile(String userId, byte[] profileBytes, DatastoreService ds) 
      throws IllegalStateException {
    Entity user = createOrGetUser(userId, ds, true);
    String profileStringBase64 = new String(profileBytes);
    String profileStringJson = new String(Base64.decode(profileStringBase64));
    
    // Trim at end where 0 chars are present.
    int count = profileStringJson.length();
    while (profileStringJson.charAt(--count) == 0);
    Gson gson = new Gson();
    System.out.println(profileStringJson.substring(0, count + 1));
    ProfileData profile = gson.fromJson(profileStringJson.substring(0, count+1), ProfileData.class);
    
    EmbeddedEntity profileEntity = createOrGetUserProfile(user, true);
    for (Map.Entry<String, String> entry: profile.properties.entrySet()) {
      if (entry.getKey().startsWith(ProfileData.PNG)) { // Too large and base64encoded - store as TEXT
        if (entry.getValue() != null) {
          profileEntity.setProperty(entry.getKey(), new Text(entry.getValue()));
        }
      } else {
        profileEntity.setProperty(entry.getKey(), entry.getValue());
      }
    }
    for (Map.Entry<String, Map<String, float[]>> topicStats: profile.topicStats.entrySet()) {
      String jsonStats = gson.toJson(topicStats.getValue());
      profileEntity.setProperty(topicStats.getKey(), new Text(jsonStats));
    }
    profileEntity.setProperty(ProfileData.SOCIAL, new Text(gson.toJson(profile.social)));
    ds.put(user);
    // If retrieved user is for requested userid and not forwarded
    if (userId.equals(user.getKey().getName())) {
      return (String) user.getProperty(OLD_USER_ID);
    }
    return null;
  }

  public static EmbeddedEntity createOrGetUserProfile(Entity user, boolean create) {
    if (user == null) return null;
    EmbeddedEntity profileEntity = (EmbeddedEntity) user.getProperty(PROFILE);
    if (create && profileEntity == null) {
      System.out.println("Creating profile for user:" + user.getKey().getName());
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
      String profileId = (String) user.getProperty(NEW_USER_ID);
      if (profileId != null && profileId.length() > 0) {
        key = KeyFactory.createKey(User.class.getSimpleName(), profileId);
        user1 = ds.get(key);
        user = user1;
      }
    } catch (EntityNotFoundException e) {
      if (create && user == null) {
        System.out.println("Creating user:" + userId.toLowerCase());
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
    String social = "{}";
    StringBuilder properties = new StringBuilder("{");
    StringBuilder topicStats = new StringBuilder("{");
    String propDelimiter = "", topicDelimiter = "";
    for (Map.Entry<String, Object> property: profileEntity.getProperties().entrySet()) {
      Object value = property.getValue();
      if (value instanceof Text) {
        String s = ((Text) value).getValue();
        if (property.getKey().startsWith(ProfileData.PNG)) {
          properties.append(propDelimiter + property.getKey() + ":\"" + s + "\"");
          propDelimiter = ",";
        } else if (property.getKey().equals(ProfileData.SOCIAL)) {
          social = s;
        } else if (!"null".equals(s)) {
          topicStats.append(topicDelimiter + property.getKey() + ":" + s);
          topicDelimiter = ",";
        }
      } else {
        properties.append(propDelimiter + property.getKey() + ":\"" + value + "\"");
        propDelimiter = ",";
      }
    }
    properties.append("}");
    topicStats.append("}");
    String json = "{ properties:" + properties + ",topicStats:" + topicStats + ",social:" + social + "}";
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
