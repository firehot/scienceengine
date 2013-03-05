package com.mazalearn.scienceengine.app.services;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.Topic;
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
  private static final String ACTIVITY = "activity";
  private static final String LAST_ACTIVITY = "last_activity";
  private static final String TOPIC = "topic";
  private static final String USER_EMAIL = "useremail";
  private static final String USER_NAME = "username";
  private static final String NUM_ATTEMPTS = "numAttempts";
  private static final String NUM_SUCCESSES = "numSucceses";
  private static final String PERCENT_ATTEMPTED = "attemptPercent";
  private static final String TIME_SPENT = "timeSpent";
  private HashMap<Topic, HashMap<String, Float>> topicStats;
  private HashMap<String, String> properties;
  private HashMap<String, Float> currentTopicStats;

  public Profile() {
    topicStats = new HashMap<Topic, HashMap<String, Float>>();
    properties = new HashMap<String, String>();
  }

  public void setUserEmail(String userEmail) {
    properties.put(USER_EMAIL, userEmail);
  }

  public void setCurrentActivity(int level) {
    String activity = properties.get(ACTIVITY);
    if (String.valueOf(level).equals(activity)) return;
    
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
    
    Object topicObj = json.readValue("topics", OrderedMap.class, OrderedMap.class, jsonData);
    topicStats = new HashMap<Topic, HashMap<String, Float>>();
    for (Topic topic: Topic.values()) {
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

  private String makeTutorKey(int activity, String tutorId, String key) {
    return activity + "$" + tutorId + "$" + key;
  }

  private String makeTutorKey(String tutorId, String key) {
    return makeTutorKey(getCurrentActivity(), tutorId, key);
  }

  private float getStat(int activity, String tutorId, String key) {
    Float value = currentTopicStats.get(makeTutorKey(activity, tutorId, key));
    return value == null ? 0 : value;
  }

  private void saveStat(String tutorId, String key, Float value) {
    String tutorKey = makeTutorKey(tutorId, key);
    if (currentTopicStats.get(tutorKey) == value) return;
    currentTopicStats.put(tutorKey, value);
    save();
  }
  
  public void save() {
    ScienceEngine.getPreferencesManager().saveProfile();
  }

  public float getTimeSpent(String tutorId) {
    return getStat(getCurrentActivity(), tutorId, TIME_SPENT);
  }
  
  public float getTimeSpent(int activity, String tutorId) {
    return getStat(activity, tutorId, TIME_SPENT);
  }

  public void setTimeSpent(String tutorId, float timeSpent) {
    saveStat(tutorId, TIME_SPENT, timeSpent);
  }

  /**
   * Get num attempts for this tutorId
   * @param tutorId
   * @return
   */
  public float getNumAttempts(String tutorId) {
    return getStat(getCurrentActivity(), tutorId, NUM_ATTEMPTS);
  }
  
  public float getNumAttempts(int activity, String tutorId) {
    return getStat(activity, tutorId, NUM_ATTEMPTS);
  }

  public void setNumAttempts(String tutorId, float numAttempts) {
    saveStat(tutorId, NUM_ATTEMPTS, numAttempts);
  }

  public float getNumSuccesses(String tutorId) {
    return getStat(getCurrentActivity(), tutorId, NUM_SUCCESSES);
  }

  public float getNumSuccesses(int activity, String tutorId) {
    return getStat(activity, tutorId, NUM_SUCCESSES);
  }

  public void setNumSuccesses(String tutorId, float numSucceses) {
    saveStat(tutorId, NUM_SUCCESSES, numSucceses);
  }

  /**
   * Get percent attempted for this tutorId
   * @param tutorId
   * @return
   */
  public float getPercentAttempted(String tutorId) {
    return getStat(getCurrentActivity(), tutorId, PERCENT_ATTEMPTED);
  }
  
  public float getPercentAttempted(int activity, String tutorId) {
    return getStat(activity, tutorId, PERCENT_ATTEMPTED);
  }


  public float getPercentAttempted(Topic topic, int level, String id) {
    HashMap<String, Float> topicProps = topicStats.get(topic);
    if (topicProps == null) return 0;
    
    Float status = topicProps.get(makeTutorKey(level, id, PERCENT_ATTEMPTED));
    return status == null ? 0 : status;
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
