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
 * system when the scienceEngine exists.
 * 
 * @see ProfileManager
 */
public class Profile implements Serializable {
  private int currentLevelId;
  private int credits;
  private Map<Integer, Integer> highScores;

  public Profile() {
    credits = 1000;
    highScores = new HashMap<Integer, Integer>();
  }

  /**
   * Retrieves the ID of the next playable level.
   */
  public int getCurrentLevelId() {
    return currentLevelId;
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

  /**
   * Notifies the score on the given level. Returns <code>true</code> if its a
   * high score.
   */
  public boolean notifyScore(int levelId, int score) {
    if (score > getHighScore(levelId)) {
      highScores.put(levelId, score);
      return true;
    }
    return false;
  }

  /**
   * Retrieves the amount of credits the player has.
   */
  public int getCredits() {
    return credits;
  }

  /**
   * Retrieves the amount of credits as text.
   */
  public String getCreditsAsText() {
    return String.valueOf(credits);
  }

  // Serializable implementation

  @SuppressWarnings("unchecked")
  @Override
  public void read(Json json, OrderedMap<String, Object> jsonData) {
    // read the some basic properties
    currentLevelId = json.readValue("currentLevelId", Integer.class, jsonData);
    credits = json.readValue("credits", Integer.class, jsonData);

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
    json.writeValue("currentLevelId", currentLevelId);
    json.writeValue("credits", credits);
    json.writeValue("highScores", highScores);
  }
}
