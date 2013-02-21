package com.mazalearn.scienceengine.app.services;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.Domain;
import com.mazalearn.scienceengine.ScienceEngine;

/**
 * The learner's profile.
 * <p>
 * This class is used to store the scienceEngine progress, and is persisted to the file
 * system when the scienceEngine exits.
 * 
 */
public class Profile implements Serializable {

  private static final String DRAWING_PNG = "DrawingPng";
  private static final String SUCCESS_PERCENT = "successPercent";
  private static final String TIME_SPENT = "timeSpent";
  private static final String ACTIVITY = "activity";
  private static final String LAST_ACTIVITY = "last_activity";
  private static final String DOMAIN = "domain";
  private static final String USER_EMAIL = "useremail";
  private static final String USER_NAME = "username";
  private HashMap<Domain, HashMap<String, String>> domainProperties;
  private HashMap<String, String> properties, currentDomain;

  public Profile() {
    domainProperties = new HashMap<Domain, HashMap<String, String>>();
    properties = new HashMap<String, String>();
  }

  public void setUserEmail(String userEmail) {
    properties.put(USER_EMAIL, userEmail);
  }

  public void setCurrentActivity(int level) {
    properties.put(LAST_ACTIVITY, properties.get(ACTIVITY));
    properties.put(ACTIVITY, String.valueOf(level));
    save();
  }
  
  /**
   * Retrieves the ID of the next playable level.
   * Stupid ligbdx converts ints to floats when writing json.
   */
  public int getCurrentActivity() {
    String levelFloatStr = properties.get(ACTIVITY);
    return  levelFloatStr == null ? 0 : Math.round(Float.valueOf(levelFloatStr));
  }

  /**
   * Retrieves the ID of the next playable level.
   * Stupid ligbdx converts ints to floats when writing json.
   */
  public int getLastActivity() {
    String levelFloatStr = properties.get(LAST_ACTIVITY);
    return  levelFloatStr == null ? 0 : Math.round(Float.valueOf(levelFloatStr));
  }

  // Serializable implementation

  @SuppressWarnings("unchecked")
  @Override
  public void read(Json json, OrderedMap<String, Object> jsonData) {

    properties = json.readValue("properties", HashMap.class, String.class, jsonData);
    if (properties == null) {
      properties = new HashMap<String,String>();
    }

    domainProperties = new HashMap<Domain, HashMap<String,String>>();
    for (Domain domain: Domain.values()) {
      HashMap<String,String> props = json.readValue(domain.name(), HashMap.class, String.class, jsonData);
      if (props == null) {
        props = new HashMap<String, String>();
      }
      domainProperties.put(domain, props);
    }
  }

  @Override
  public void write(Json json) {
    json.writeValue("properties", properties);
    for (Domain domain: Domain.values()) {
      HashMap<String,String> props = domainProperties.get(domain);
      json.writeValue(domain.name(), props);
    }
  }

  public void setCurrentDomain(Domain domain) {
    properties.put(DOMAIN, domain != null ? domain.name() : null);
    currentDomain = domainProperties.get(domain);
    if (currentDomain == null) {
      currentDomain = new HashMap<String,String>();
      domainProperties.put(domain, currentDomain);
    }
    save();
  }

  public Domain getCurrentDomain() {
    String s = properties.get(DOMAIN);
    return s == null || s.length() == 0 ? null : Domain.valueOf(s);
  }

  public void setUserName(String name) {
    properties.put(USER_NAME, name);
  }

  public String getUserName() {
    String s = properties.get(USER_NAME);
    return s == null ? "" : s;
  }

  public String getUserEmail() {
    String s = properties.get(USER_EMAIL);
    return s == null ? "" : s;
  }

  public float getTimeSpent(String subgoalId) {
    return getTimeSpent(getCurrentActivity(), subgoalId);
  }
  
  public float getTimeSpent(int activity, String tutorId) {
    String timeSpentStr = currentDomain.get(makeTutorKey(activity, tutorId, TIME_SPENT));
    return timeSpentStr == null ? 0 : Float.valueOf(timeSpentStr);
  }

  private String makeTutorKey(int activity, String tutorId, String key) {
    return activity + "$" + tutorId + "$" + key;
  }

  private String makeTutorKey(String tutorId, String key) {
    return makeTutorKey(getCurrentActivity(), tutorId, key);
  }

  public void setTimeSpent(String tutorId, float timeSpent) {
    currentDomain.put(makeTutorKey(tutorId, TIME_SPENT), 
        String.valueOf(timeSpent));
  }
  
  public void save() {
    ScienceEngine.getPreferencesManager().saveProfile();
  }

  /**
   * Get percent success for this subgoalId
   * @param tutorId
   * @return
   */
  public int getSuccessPercent(String tutorId) {
    return getSuccessPercent(getCurrentActivity(), tutorId);
  }
  
  public int getSuccessPercent(int activity, String tutorId) {
    String status = currentDomain.get(makeTutorKey(activity, tutorId, SUCCESS_PERCENT));
    try {
      return status != null ? Integer.parseInt(status) : 0;
    } catch(NumberFormatException e) {
      return 0;
    }
  }


  public int getSuccessPercent(Domain domain, int level, String id) {
    HashMap<String, String> domainProps = domainProperties.get(domain);
    if (domainProps == null) return 0;
    
    String status = domainProps.get(makeTutorKey(level, id, SUCCESS_PERCENT));
    try {
      return status != null ? Integer.parseInt(status) : 0;
    } catch(NumberFormatException e) {
      return 0;
    }
  }

  public void setSuccessPercent(String tutorId, int percent) {
    currentDomain.put(makeTutorKey(tutorId, SUCCESS_PERCENT), String.valueOf(percent));
    save();
  }

  public void setDrawingPng(byte[] drawingPngBytes) {
    try {
      properties.put(DRAWING_PNG, new String(drawingPngBytes, "US_ASCII"));
    } catch (UnsupportedEncodingException ignored) {
    }
    save();
  }
  
  public byte[] getDrawingPng() {
    String png = properties.get(DRAWING_PNG);
    try {
      return png == null ? new byte[0] : png.getBytes("US_ASCII");
    } catch (UnsupportedEncodingException e) {
      return new byte[0];
    }
  }
}
