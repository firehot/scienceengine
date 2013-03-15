package com.mazalearn.scienceengine.app.services;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.tutor.TutorStats;

/**
 * The learner's profile.
 * <p>
 * This class is used to store the scienceEngine progress, and is persisted to the file
 * system when the scienceEngine exits.
 * 
 */
public class Profile implements Serializable {

  private static final String DRAWING_PNG = "DrawingPng";
  private static final String ACTIVITY = "activity";
  private static final String LAST_ACTIVITY = "last_activity";
  private static final String TOPIC = "topic";
  private static final String USER_EMAIL = "useremail";
  private static final String USER_NAME = "username";
  private static final String NUM_ATTEMPTS = "numAttempts";
  private static final String NUM_SUCCESSES = "numSucceses";
  private static final String PERCENT_PROGRESS = "percentProgress";
  private static final String TIME_SPENT = "timeSpent";
  private static final String FAILURE_TRACKER = "failureTracker";
  private static final String INSTALL_ID = "install_id";
  
  private HashMap<Topic, HashMap<String, Float>> topicStats;
  private HashMap<String, String> properties;
  private HashMap<String, Float> currentTopicStats;

  public Profile() {
    topicStats = new HashMap<Topic, HashMap<String, Float>>();
    for (Topic topic: Topic.values()) {
      if (topic.getChildren().length == 0) continue;
      topicStats.put(topic, new HashMap<String, Float>());
    }
    properties = new HashMap<String, String>();
    properties.put(INSTALL_ID, ScienceEngine.getPlatformAdapter().getInstallationId());
  }

  public void setUserEmail(String userEmail) {
    properties.put(USER_EMAIL, userEmail);
  }

  public void setCurrentActivity(Topic level) {
    Topic activity = getCurrentActivity();
    if (level == activity) return;
    
    properties.put(LAST_ACTIVITY, activity != null ? activity.name() : "");
    properties.put(ACTIVITY, level != null ? level.name() : "");
    save();
  }
  
  /**
   * Retrieves the ID of the current level.
   */
  public Topic getCurrentActivity() {
    String levelStr = properties.get(ACTIVITY);
    if (levelStr == null) return null;
    try {
      return Topic.valueOf(levelStr);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Retrieves the ID of the previous active level.
   */
  public Topic getLastActivity() {
    String levelStr = properties.get(LAST_ACTIVITY);
    if (levelStr == null) return null;
    try {
      return Topic.valueOf(levelStr);
    } catch (IllegalArgumentException e) {
      return null;
    }
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
    
    Object topicObj = json.readValue("topics", OrderedMap.class, OrderedMap.class, jsonData);
    topicStats = new HashMap<Topic, HashMap<String, Float>>();
    for (Topic topic: Topic.values()) {
      if (topic.getChildren().length == 0) continue;
      HashMap<String, Float> stats = json.readValue(topic.name(), HashMap.class, Float.class, topicObj);
      if (stats == null) {
        stats = new HashMap<String, Float>();
      }
      topicStats.put(topic, stats);
    }
    // Set current topic
    Topic currentTopic = Topic.Electromagnetism;
    try {
      currentTopic = Topic.valueOf(properties.get(TOPIC));
    } catch (Exception ignored) {}
    currentTopicStats = topicStats.get(currentTopic);
  }

  @Override
  public void write(Json json) {
    json.writeValue("properties", properties);
    json.writeObjectStart("topics");
    for (Topic topic: Topic.values()) {
      HashMap<String,?> props = topicStats.get(topic);
      json.writeValue(topic.name(), props);
    }
    json.writeObjectEnd();
  }

  public void setCurrentTopic(Topic topic) {
    if (topic != null && topic.name().equals(properties.get(TOPIC))) return;
    properties.put(TOPIC, topic != null ? topic.name() : null);
    currentTopicStats = topicStats.get(topic);
    if (currentTopicStats == null) {
      currentTopicStats = new HashMap<String, Float>();
      topicStats.put(topic, currentTopicStats);
    }
    save();
  }
  
  public Topic getCurrentTopic() {
    String s = properties.get(TOPIC);
    return s == null || s.length() == 0 ? null : Topic.valueOf(s);
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

  private String makeTutorKey(Topic level, String tutorId, String key) {
    return level + "$" + tutorId + "$" + key;
  }

  private float getStat(Topic topic, Topic level, String tutorId, String key) {
    HashMap<String, Float> topicStat = topicStats.get(topic);
    Float value = topicStat.get(makeTutorKey(level, tutorId, key));
    return value == null ? 0 : value;
  }

  private void saveStat(String tutorId, String key, Float value) {
    String tutorKey = makeTutorKey(getCurrentActivity(), tutorId, key);
    if (currentTopicStats.get(tutorKey) == value) return;
    currentTopicStats.put(tutorKey, value);
  }
  
  public void save() {
    ScienceEngine.getPreferencesManager().saveProfile();
  }

  public void setDrawingPng(byte[] drawingPngBytes) {
    try {
      properties.put(DRAWING_PNG, new String(drawingPngBytes, "US-ASCII"));
    } catch (UnsupportedEncodingException ignored) {
    }
    save();
  }
  
  public byte[] getDrawingPng() {
    String png = properties.get(DRAWING_PNG);
    try {
      return png == null ? new byte[0] : png.getBytes("US-ASCII");
    } catch (UnsupportedEncodingException e) {
      return new byte[0];
    }
  }

  public void loadStats(TutorStats stats, Topic topic, Topic level, String tutorId) {
    stats.timeSpent = getStat(topic, level, tutorId, TIME_SPENT);
    stats.numAttempts = getStat(topic, level, tutorId, NUM_ATTEMPTS);
    stats.numSuccesses = getStat(topic, level, tutorId, NUM_SUCCESSES);
    stats.failureTracker = getStat(topic, level, tutorId, FAILURE_TRACKER);
    stats.percentProgress = getStat(topic, level, tutorId, PERCENT_PROGRESS);
    Gdx.app.log(ScienceEngine.LOG, stats.toString());
  }

  /**
   * Save stats for current topic, current activity.
   * @param stats
   * @param tutorId
   */
  public void saveStats(TutorStats stats, String tutorId) {
    saveStat(tutorId, TIME_SPENT, stats.timeSpent);
    saveStat(tutorId, NUM_ATTEMPTS, stats.numAttempts);
    saveStat(tutorId, NUM_SUCCESSES, stats.numSuccesses);
    saveStat(tutorId, FAILURE_TRACKER, stats.failureTracker);
    saveStat(tutorId, PERCENT_PROGRESS, stats.percentProgress);
    save();
    Gdx.app.log(ScienceEngine.LOG, stats.toString());
  }
}
