package com.mazalearn.gwt.server;

import java.lang.reflect.Constructor;

import com.badlogic.gdx.Gdx;
import com.google.appengine.api.datastore.PropertyContainer;
import com.google.appengine.api.datastore.Text;
import com.google.gson.Gson;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.InstallData;
import com.mazalearn.scienceengine.app.services.ProfileData;
import com.mazalearn.scienceengine.app.utils.Crypter;

public class JsonEntityUtil {

  private Gson gson = new Gson();

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <T> T getFromJsonTextProperty(PropertyContainer entity, String name, Class<T> clz) {
    if (entity == null) return null;
    
    Text objectJson = (Text) entity.getProperty(name);
    try {
      if (objectJson != null) {
        return gson.fromJson(objectJson.getValue(), clz);
      }
      Constructor c = clz.getConstructor(new Class[0]);
      return (T) c.newInstance(new Object[0]);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public void setAsJsonTextProperty(
      PropertyContainer entity, String name, Object javaObj) {
    entity.setProperty(name, new Text(gson.toJson(javaObj)));
  }

  public ProfileData profileFromJson(String clientProfileJson) {
    // causes NumberFormatException if present
    clientProfileJson = clientProfileJson.replace("class:\"java.util.HashMap\",",  "");
    clientProfileJson = clientProfileJson.replace("class:\"java.util.HashMap\"",  "");
    return gson.fromJson(clientProfileJson, ProfileData.class);
  }

  public ProfileData profileFromBase64(byte[] profileBytes) {
    String profileBase64 = new String(profileBytes);
    String profileJson = new String(Base64.decode(profileBase64));
    
    // Trim at end where 0 chars are present.
    int count = profileJson.length();
    while (profileJson.charAt(--count) == 0);
    System.out.println("UserProfile:" + profileJson.substring(0, count + 1));
    // Get profile data
    return profileFromJson(profileJson.substring(0, count+1));
  }

  public InstallData installProfileFromBase64(byte[] profileBytes, String installId) {
    String profileBase64AndHash = new String(profileBytes);
    // decode the contents - hash is last 40 bytes
    String profileBase64 = profileBase64AndHash.substring(0, profileBase64AndHash.length() - 40);
    String hashReceived = profileBase64AndHash.substring(profileBase64AndHash.length() - 40);
    // Verify hash
    String hashCalculated = Crypter.saltedSha1Hash(profileBase64, installId);
    if (!hashCalculated.equals(hashReceived)) {
      System.out.println("Install profile - Hash mismatch: " + hashCalculated + " " + hashReceived);
      return null;
    }
    String profileJson = new String(Base64.decode(profileBase64));
    
    // Trim at end where 0 chars are present.
    int count = profileJson.length();
    while (profileJson.charAt(--count) == 0);
    System.out.println("InstallProfile:" + profileJson.substring(0, count + 1));
    // Get profile data
    return gson.fromJson(profileJson.substring(0, count+1), InstallData.class);
  }

}
