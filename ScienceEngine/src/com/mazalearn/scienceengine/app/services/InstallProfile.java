package com.mazalearn.scienceengine.app.services;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.SerializationException;
import com.mazalearn.scienceengine.ScienceEngine;

/**
 * The installation profile.
 * <p>
 * This class is used to store the profile of the installation, and is persisted to the file
 * system when the scienceEngine exits.
 * 
 */
public class InstallProfile implements Serializable {

  private static final String PNG = "png";
  private static final String ENTERPRISE_NAME = "enterprisename";
  private static final String ENTERPRISE_LOGO = PNG + "enterprise";
  private static final String ENTERPRISE_ID = "enterpriseid";
  public static final String INSTALL_ID = "installid";
  public static final String INSTALL_NAME = "installname";
  private static final String LAST_UPDATED = "last_updated";
  
  private HashMap<String, String> properties;
  private String[] userids;
  
  public InstallProfile() {
    properties = new HashMap<String, String>();
    properties.put(INSTALL_ID, ScienceEngine.getPlatformAdapter().getInstallationId());
  }

    // Serializable implementation

  @SuppressWarnings("unchecked")
  @Override
  public void read(Json json, OrderedMap<String, Object> jsonData) {

    properties = json.readValue("properties", HashMap.class, String.class, jsonData);
    if (properties == null) {
      properties = new HashMap<String,String>();
      properties.put(INSTALL_ID, ScienceEngine.getPlatformAdapter().getInstallationId());
    }
    userids = json.readValue("userids", String[].class, new String[]{}, jsonData);
  }

  @Override
  public void write(Json json) {
    json.writeValue("properties", properties);
    json.writeValue("userids", userids);
  }

  public String getEnterpriseName() {
    String s = properties.get(ENTERPRISE_NAME);
    return s == null ? "" : s;
  }

  public String getEnterpriseId() {
    String s = properties.get(ENTERPRISE_ID);
    return s == null ? "" : s;
  }

  public void save() {
    properties.put(LAST_UPDATED, String.valueOf(System.currentTimeMillis()));
    ScienceEngine.getPreferencesManager().saveInstallProfile();
  }
  
  public long getLastUpdated() {
    try {
      String lastUpdated = properties.get(LAST_UPDATED);
      return Long.parseLong(lastUpdated);
    } catch (IllegalArgumentException e) {
      return 0;
    }
  }

  public Pixmap getEnterpriseLogo() {
    String png = properties.get(ENTERPRISE_LOGO);
    return png == null ? null : ScienceEngine.getPlatformAdapter().bytes2Pixmap(Base64Coder.decode(png));
  }

  public String[] getUserIds() {    
    return userids;
  }

  public String getInstallationId() {
    return properties.get(INSTALL_ID);
  }

  public String getInstallationName() {
    return properties.get(INSTALL_NAME);
  }

  public static InstallProfile fromBase64(String profileBase64) {
    InstallProfile profile = null;
    if (profileBase64 != null && profileBase64.length() > 0) {
      // decode the contents - base64 encoded
      String profileJson = Base64Coder.decodeString(profileBase64);
      try {
        profile = new Json().fromJson(InstallProfile.class, profileJson);
      } catch (SerializationException s) {
        Gdx.app.error(ScienceEngine.LOG, "Error deserializing: " + s.getMessage() + "\n" + profileJson);
      } catch (IllegalArgumentException s) {
        Gdx.app.error(ScienceEngine.LOG, "Error deserializing: " + s.getMessage() + "\n" + profileJson);
      }
    }
    return profile;
  }

  public String toBase64() {
    String profileAsText = new Json(OutputType.json).toJson(this);
    Gdx.app.log(ScienceEngine.LOG, "Saving InstallProfile - " + profileAsText);
    String profileAsBase64 = Base64Coder.encodeString(profileAsText);
    return profileAsBase64;
  }
}
