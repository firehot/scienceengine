package com.mazalearn.gwt.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
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
import com.google.gson.reflect.TypeToken;

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
    Map<String, Map<String, String>> domains;
  };

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
    
    int count = profileStringJson.length();
    while (profileStringJson.charAt(--count) == 0);
    System.out.println((int) profileStringJson.charAt(count));
    System.out.println("User " + userEmail + " saved profile: \n<" + profileStringJson.substring(0, count + 1) + ">");
    Gson gson = new Gson();
    //Profile profile = gson.fromJson(profileStringJson.substring(0, count + 1), Profile.class);
    Type fooType = new TypeToken<Map<String, Map<String,String>>>() {}.getType();
    Map<String, Map<String, String>> props = gson.fromJson(profileStringJson.substring(0, count+1), fooType);
    
    EmbeddedEntity profileEntity = (EmbeddedEntity) user.getProperty(Profile.class.getSimpleName());
    if (profileEntity == null) {
      profileEntity = new EmbeddedEntity();
      user.setProperty(Profile.class.getSimpleName(), profileEntity);
    }
    for (Map.Entry<String, Map<String, String>> entry: props.entrySet()) {
      //profileEntity.setProperty(entry.getKey(), entry.getValue());
      if ("properties".equals(entry.getKey())) {
        for (Map.Entry<String, String> item: entry.getValue().entrySet()) {
          profileEntity.setProperty(item.getKey(), item.getValue());
          System.out.println("Property: " + item.getKey());
        }
      } else { // Store in JSON form
        String json = gson.toJson(entry.getValue());
        profileEntity.setProperty(entry.getKey(), new Text(json));
        System.out.println("Domain: " + entry.getKey() + " -> " + json);
      }
    }
    ds.put(user);
    System.out.println(user.toString());
  }
  

}
