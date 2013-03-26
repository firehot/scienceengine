package com.mazalearn.scienceengine.app.services;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter.Platform;
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
  private static final String INSTALL_ID = "install_id";
  private static final String LAST_UPDATED = "last_updated";
  private static final String CURRENT = "current";
  private static final String COLOR = "color";
  private static final String PLATFORM = "platform";
  
  private HashMap<Topic, HashMap<String, float[]>> topicStats;
  private HashMap<String, String> properties;
  private HashMap<String, float[]> currentTopicStats;

  public Profile() {
    topicStats = new HashMap<Topic, HashMap<String, float[]>>();
    for (Topic topic: Topic.values()) {
      if (topic.getChildren().length == 0) continue;
      topicStats.put(topic, new HashMap<String, float[]>());
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
    topicStats = new HashMap<Topic, HashMap<String, float[]>>();
    for (Topic topic: Topic.values()) {
      if (topic.getChildren().length == 0) continue;
      HashMap<String, float[]> stats = json.readValue(topic.name(), HashMap.class, float[].class, topicObj);
      if (stats == null) {
        stats = new HashMap<String, float[]>();
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
      if (props != null) {
        json.writeValue(topic.name(), props);
      }
    }
    json.writeObjectEnd();
  }

  public void setCurrentTopic(Topic topic) {
    if (topic != null && topic.name().equals(properties.get(TOPIC))) return;
    properties.put(TOPIC, topic != null ? topic.name() : null);
    currentTopicStats = topicStats.get(topic);
    if (currentTopicStats == null) {
      currentTopicStats = new HashMap<String, float[]>();
      topicStats.put(topic, currentTopicStats);
    }
    save();
  }
  
  public Topic getCurrentTopic() {
    String s = properties.get(TOPIC);
    try {
      return s == null || s.length() == 0 ? null : Topic.valueOf(s);
    } catch (IllegalArgumentException e) {
      return null;
    }
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

  private String makeTutorKey(Topic level, String tutorId) {
    return level.getTopicId() + "$" + tutorId;
  }

  public void save() {
    properties.put(LAST_UPDATED, String.valueOf(System.currentTimeMillis()));
    ScienceEngine.getPreferencesManager().saveProfile();
  }
  
  public long getLastUpdated() {
    try {
      String lastUpdated = properties.get(LAST_UPDATED);
      return Long.parseLong(lastUpdated);
    } catch (IllegalArgumentException e) {
      return 0;
    }
  }

  public void setDrawingPng(byte[] drawingPngBytes, String current, String color) {
    Gdx.app.error(ScienceEngine.LOG, " bytes = " + drawingPngBytes.length);
    properties.put(DRAWING_PNG, new String(Base64Coder.encode(drawingPngBytes)));
    properties.put(CURRENT, current);
    properties.put(COLOR, color);
    save();
  }
  
  public byte[] getDrawingPng() {
    String png = properties.get(DRAWING_PNG);
    return png == null ? new byte[0] : Base64Coder.decode(png);
  }

  public void loadStats(TutorStats stats, Topic topic, Topic level, String tutorId) {
    HashMap<String, float[]> topicStat = topicStats.get(topic);
    float[] s = topicStat.get(makeTutorKey(level, tutorId));
    if (s != null) stats.stats = s;
    Gdx.app.log(ScienceEngine.LOG, stats.toString());
  }

  /**
   * Save stats for current topic, current activity.
   * @param stats
   * @param tutorId
   */
  public void saveStats(TutorStats stats, String tutorId) {
    String tutorKey = makeTutorKey(getCurrentActivity(), tutorId);
    currentTopicStats.put(tutorKey, stats.stats);
    save();
    Gdx.app.log(ScienceEngine.LOG, stats.toString());
  }

  public void setPlatform(Platform platform) {
    properties.put(PLATFORM, platform.name());
  }
}
