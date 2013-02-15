package com.mazalearn.scienceengine.app.services;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.OrderedMap;

/**
 * The player's profile.
 * <p>
 * This class is used to store the scienceEngine progress, and is persisted to the file
 * system when the scienceEngine exits.
 * 
 * @see ProfileManager
 */
public class Profile implements Serializable {
  private static final String ACTIVITY = "activity";
  private static final String DOMAIN = "domain";
  private static final String USER_EMAIL = "useremail";
  private static final String USER_NAME = "username";
  private Map<Integer, Integer> highScores;
  private Map<String, String> properties;

  public Profile() {
    highScores = new HashMap<Integer, Integer>();
    properties = new HashMap<String, String>();
  }

  public void setCurrentActivity(int level) {
    properties.put(ACTIVITY, String.valueOf(level));
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

  public void setDomain(String name) {
    properties.put(DOMAIN, name);
  }

  public String getDomain() {
    String s = properties.get(DOMAIN);
    return s == null ? "" : s;
  }

  public boolean getLocked(int iLevel) {
    return false;
  }

  public void setUserName(String name) {
    properties.put(USER_NAME, name);
  }

  public void setUserEmail(String email) {
    properties.put(USER_EMAIL, email);
  }

  public String getUserName() {
    String s = properties.get(USER_NAME);
    return s == null ? "" : s;
  }

  public String getUserEmail() {
    String s = properties.get(USER_EMAIL);
    return s == null ? "" : s;
  }
}
