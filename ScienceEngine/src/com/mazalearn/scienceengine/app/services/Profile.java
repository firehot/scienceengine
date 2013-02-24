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
  private HashMap<Domain, HashMap<String, Float>> domainStats;
  private HashMap<String, String> properties;
  private HashMap<String, Float> currentDomainStats;

  public Profile() {
    domainStats = new HashMap<Domain, HashMap<String, Float>>();
    properties = new HashMap<String, String>();
  }

  public void setUserEmail(String userEmail) {
    properties.put(USER_EMAIL, userEmail);
  }

  public void setCurrentActivity(int level) {
    String activity = properties.get(ACTIVITY);
    if (activity.equals(String.valueOf(level))) return;
    
    properties.put(LAST_ACTIVITY, activity);
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
    
    Object domainObj = json.readValue("domains", OrderedMap.class, OrderedMap.class, jsonData);
    domainStats = new HashMap<Domain, HashMap<String, Float>>();
    for (Domain domain: Domain.values()) {
      HashMap<String, Float> stats = json.readValue(domain.name(), HashMap.class, Float.class, domainObj);
      if (stats == null) {
        stats = new HashMap<String, Float>();
      }
      domainStats.put(domain, stats);
    }
    // Set current domain
    Domain currentDomain = Domain.valueOf(properties.get(DOMAIN));
    currentDomainStats = domainStats.get(currentDomain);
  }

  @Override
  public void write(Json json) {
    json.writeValue("properties", properties);
    json.writeObjectStart("domains");
    for (Domain domain: Domain.values()) {
      HashMap<String,?> props = domainStats.get(domain);
      json.writeValue(domain.name(), props);
    }
    json.writeObjectEnd();
  }

  public void setCurrentDomain(Domain domain) {
    if (domain.name().equals(properties.get(DOMAIN))) return;
    properties.put(DOMAIN, domain != null ? domain.name() : null);
    currentDomainStats = domainStats.get(domain);
    if (currentDomainStats == null) {
      currentDomainStats = new HashMap<String, Float>();
      domainStats.put(domain, currentDomainStats);
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

  public float getTimeSpent(String tutorId) {
    return getTimeSpent(getCurrentActivity(), tutorId);
  }
  
  public float getTimeSpent(int activity, String tutorId) {
    Float timeSpent = (Float) currentDomainStats.get(makeTutorKey(activity, tutorId, TIME_SPENT));
    return timeSpent == null ? 0 : timeSpent;
  }

  private String makeTutorKey(int activity, String tutorId, String key) {
    return activity + "$" + tutorId + "$" + key;
  }

  private String makeTutorKey(String tutorId, String key) {
    return makeTutorKey(getCurrentActivity(), tutorId, key);
  }

  public void setTimeSpent(String tutorId, float timeSpent) {
    saveStat(makeTutorKey(tutorId, TIME_SPENT), timeSpent);
  }

  private void saveStat(String tutorKey, Float value) {
    if (currentDomainStats.get(tutorKey) == value) return;
    currentDomainStats.put(tutorKey, value);
  }
  
  public void save() {
    ScienceEngine.getPreferencesManager().saveProfile();
  }

  /**
   * Get percent success for this tutorId
   * @param tutorId
   * @return
   */
  public float getSuccessPercent(String tutorId) {
    return getSuccessPercent(getCurrentActivity(), tutorId);
  }
  
  public float getSuccessPercent(int activity, String tutorId) {
    Float status = currentDomainStats.get(makeTutorKey(activity, tutorId, SUCCESS_PERCENT));
    return status == null ? 0 : status;
  }


  public float getSuccessPercent(Domain domain, int level, String id) {
    HashMap<String, Float> domainProps = domainStats.get(domain);
    if (domainProps == null) return 0;
    
    Float status = domainProps.get(makeTutorKey(level, id, SUCCESS_PERCENT));
    return status == null ? 0 : status;
  }

  public void setSuccessPercent(String tutorId, float percent) {
    saveStat(makeTutorKey(tutorId, SUCCESS_PERCENT), percent);
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
