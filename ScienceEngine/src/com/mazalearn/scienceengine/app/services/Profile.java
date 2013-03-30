package com.mazalearn.scienceengine.app.services;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter.Platform;
import com.mazalearn.scienceengine.tutor.ITutor;

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
  public static final String USER_ID = "userid"; // readonly
  private static final String USER_NAME = "username"; // readonly
  public static final String INSTALL_ID = "installid";
  private static final String LAST_UPDATED = "last_updated";
  private static final String CURRENT = "current";
  private static final String COLOR = "color";
  private static final String PLATFORM = "platform";
  public static final String USER_EMAIL = "useremail";
  
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

  public String getUserName() {
    String s = properties.get(USER_NAME);
    return s == null ? "" : s;
  }

  public String getUserEmail() {
    String s = properties.get(USER_ID);
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

  public float[] getStats(Topic topic, Topic level, String tutorId) {
    HashMap<String, float[]> topicStat = topicStats.get(topic);
    float[] s = topicStat.get(makeTutorKey(level, tutorId));
    return (s != null) ? s : new float[ITutor.NUM_STATS];
  }

  public float[] getStats(String tutorId) {
    return getStats(getCurrentTopic(), getCurrentActivity(), tutorId);
  }
  /**
   * Save stats for current topic, current activity.
   * @param stats
   * @param tutorId
   */
  public void saveStats(float[] stats, String tutorId) {
    String tutorKey = makeTutorKey(getCurrentActivity(), tutorId);
    currentTopicStats.put(tutorKey, stats);
    save();
  }

  public void setPlatform(Platform platform) {
    properties.put(PLATFORM, platform.name());
  }

  public String getInstallationId() {
    return properties.get(INSTALL_ID);
  }

  private static Profile merge(Profile profile1, Profile profile2) {
    if (profile1.getLastUpdated() >= profile2.getLastUpdated()) {
      profile2.properties.putAll(profile1.properties);
      return profile2;
    } else {
      profile1.properties.putAll(profile2.properties);
      return profile1;
    }
  }

  public static Profile mergeProfiles(String localProfileBase64,
      String serverProfileBase64) {
    Profile localProfile = null;
    if (localProfileBase64 != null && localProfileBase64.length() > 0) {
      // decode the contents - base64 encoded
      String localProfileStr = Base64Coder.decodeString(localProfileBase64);
      localProfile = new Json().fromJson(Profile.class, localProfileStr);
    }
    // Retrieve from server if available
    Profile serverProfile = null;
    if (serverProfileBase64 != null && serverProfileBase64.length() > 0) {
      // decode the contents - base64 encoded
      String serverProfileStr = Base64Coder.decodeString(serverProfileBase64);
      serverProfile = new Json().fromJson(Profile.class, serverProfileStr);
    }
    // Choose latest available profile or create a new one if none available
    if (localProfile != null && serverProfile != null) {
      return merge(localProfile, serverProfile);
    } else if (localProfile != null) {
      return localProfile;
    } else if (serverProfile != null) {
      return serverProfile;
    }
    return new Profile();
  }

  public String getBase64() {
    String profileAsText = new Json(OutputType.json).toJson(this);
    Gdx.app.log(ScienceEngine.LOG, "Saving Profile - " + profileAsText);
    String profileAsBase64 = Base64Coder.encodeString(profileAsText);
    return profileAsBase64;
  }
}
