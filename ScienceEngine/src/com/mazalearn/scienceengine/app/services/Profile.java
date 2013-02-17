package com.mazalearn.scienceengine.app.services;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.OrderedMap;
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
  private static final String STATUS = "status";
  private static final String TIME_SPENT = "timeSpent";
  private static final String ACTIVITY = "activity";
  private static final String LAST_ACTIVITY = "last_activity";
  private static final String DOMAIN = "domain";
  private static final String LAST_DOMAIN = "domain";
  private static final String USER_EMAIL = "useremail";
  private static final String USER_NAME = "username";
  private Map<Integer, Integer> highScores;
  private Map<String, String> properties;

  public Profile() {
    highScores = new HashMap<Integer, Integer>();
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

  /**
   * Retrieves the high scores for each level (Level-ID -> High score).
   */
  public Map<Integer, Integer> getHighScores() {
    return highScores;
  }

  /**
   * Gets the current high score for the given level.
   */
  public int getHighScore(int levelId) {
    if (highScores == null)
      return 0;
    Integer highScore = highScores.get(levelId);
    return (highScore == null ? 0 : highScore);
  }

  // Serializable implementation

  @SuppressWarnings("unchecked")
  @Override
  public void read(Json json, OrderedMap<String, Object> jsonData) {

    properties = json.readValue("properties", HashMap.class, String.class, jsonData);
    if (properties == null) {
      properties = new HashMap<String, String>();
    }
    // libgdx handles the keys of JSON formatted HashMaps as Strings, but we
    // want it to be an integer instead (levelId)
    Map<String, Integer> highScores = json.readValue("highScores",
        HashMap.class, Integer.class, jsonData);
    for (String levelIdAsString : highScores.keySet()) {
      int levelId = Integer.valueOf(levelIdAsString);
      Integer highScore = highScores.get(levelIdAsString);
      this.highScores.put(levelId, highScore);
    }
  }

  @Override
  public void write(Json json) {
    json.writeValue("properties", properties);
    json.writeValue("highScores", highScores);
  }

  public void setCurrentDomain(String name) {
    properties.put(LAST_DOMAIN, getLastDomain());
    properties.put(DOMAIN, name);
    save();
  }

  public String getCurrentDomain() {
    String s = properties.get(DOMAIN);
    return s == null ? "" : s;
  }

  public String getLastDomain() {
    String s = properties.get(LAST_DOMAIN);
    return s == null ? "" : s;
  }

  public void setUserName(String name) {
    properties.put(USER_NAME, name);
    save();
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
    String timeSpentStr = properties.get(makeSubgoalKey(subgoalId, TIME_SPENT));
    return timeSpentStr == null ? 0 : Float.valueOf(timeSpentStr);
  }

  private String makeSubgoalKey(String subgoalId, String key) {
    return getCurrentDomain() + "/" + 
        getCurrentActivity() + "/" + subgoalId + "/" + key;
  }

  public void setTimeSpent(String subgoalId, float timeSpent) {
    properties.put(makeSubgoalKey(subgoalId, TIME_SPENT), 
        String.valueOf(timeSpent));
  }
  
  public void save() {
    ScienceEngine.getPreferencesManager().saveProfile();
  }

  public int getSuccessPercent(String subgoalId) {
    String status = properties.get(makeSubgoalKey(subgoalId, STATUS));
    try {
      return status != null ? Integer.parseInt(status) : 0;
    } catch(NumberFormatException e) {
      return 0;
    }
  }

  public void setSuccessPercent(String subgoalId, int percent) {
    properties.put(makeSubgoalKey(subgoalId, STATUS), String.valueOf(percent));
    save();
  }

  public void setDrawingPng(byte[] drawingPngBytes) {
    try {
      properties.put(DRAWING_PNG, new String(drawingPngBytes, "ISO-8859-1"));
    } catch (UnsupportedEncodingException ignored) {
    }
    save();
  }
  
  public byte[] getDrawingPng() {
    String png = properties.get(DRAWING_PNG);
    try {
      return png == null ? new byte[0] : png.getBytes("ISO-8859-1");
    } catch (UnsupportedEncodingException e) {
      return new byte[0];
    }
  }
}
