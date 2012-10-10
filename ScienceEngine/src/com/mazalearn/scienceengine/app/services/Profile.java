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
  private Map<Integer, Integer> highScores;
  private Map<String, String> properties;

  public Profile() {
    highScores = new HashMap<Integer, Integer>();
    properties = new HashMap<String, String>();
  }

  public void setCurrentLevel(int level) {
    properties.put("level", String.valueOf(level));
  }
  
  /**
   * Retrieves the ID of the next playable level.
   * Stupid ligbdx converts ints to floats when writing json.
   */
  public int getCurrentLevel() {
    String levelFloatStr = properties.get("level");
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

  public void setExperiment(String name) {
    properties.put("experiment", name);
  }

  public String getExperiment() {
    String s = properties.get("experiment");
    return s == null ? "" : s;
  }
}
