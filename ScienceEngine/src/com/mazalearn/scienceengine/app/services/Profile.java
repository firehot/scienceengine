package com.mazalearn.scienceengine.app.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.SerializationException;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.services.ProfileData.Social.Message;
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

  private ProfileData data = new ProfileData();

  public Profile() {
    data.topicStats = new HashMap<String, Map<String, float[]>>();
    for (Topic topic: Topic.values()) {
      if (topic.getChildren().length == 0) continue;
      data.topicStats.put(topic.name(), new HashMap<String, float[]>());
    }
    data.properties = new HashMap<String, String>();
    data.properties.put(ProfileData.INSTALL_ID, ScienceEngine.getPlatformAdapter().getInstallationId());
    data.social = new ProfileData.Social();
  }

  public void setCurrentActivity(Topic level) {
    Topic activity = getCurrentActivity();
    if (level == activity) return;
    
    data.properties.put(ProfileData.LAST_ACTIVITY, activity != null ? activity.name() : "");
    data.properties.put(ProfileData.ACTIVITY, level != null ? level.name() : "");
    save();
  }
  
  /**
   * Retrieves the ID of the current level.
   */
  public Topic getCurrentActivity() {
    String levelStr = data.properties.get(ProfileData.ACTIVITY);
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
    String levelStr = data.properties.get(ProfileData.LAST_ACTIVITY);
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

    data.properties = json.readValue("properties", HashMap.class, String.class, jsonData);
    if (data.properties == null) {
      data.properties = new HashMap<String,String>();
      data.properties.put(ProfileData.INSTALL_ID, ScienceEngine.getPlatformAdapter().getInstallationId());
    }
    
    Object topicObj = json.readValue("topicStats", OrderedMap.class, OrderedMap.class, jsonData);
    data.topicStats = new HashMap<String, Map<String, float[]>>();
    for (Topic topic: Topic.values()) {
      if (topic.getChildren().length == 0) continue;
      HashMap<String, float[]> stats = json.readValue(topic.name(), HashMap.class, float[].class, topicObj);
      if (stats == null) {
        stats = new HashMap<String, float[]>();
      }
      data.topicStats.put(topic.name(), stats);
    }
    data.social = json.readValue("social", ProfileData.Social.class, jsonData);
    
    // Set current topic
    Topic currentTopic = Topic.Electromagnetism;
    try {
      currentTopic = Topic.valueOf(data.properties.get(ProfileData.TOPIC));
    } catch (Exception ignored) {}
    data.currentTopicStats = data.topicStats.get(currentTopic.name());
  }

  @Override
  public void write(Json json) {
    json.writeValue("properties", data.properties);
    json.writeObjectStart("topicStats");
    for (Topic topic: Topic.values()) {
      Map<String,?> props = data.topicStats.get(topic.name());
      if (props != null) {
        json.writeValue(topic.name(), props);
      }
    }
    json.writeObjectEnd();
    json.writeValue("social", data.social);
  }

  public void setCurrentTopic(Topic topic) {
    if (topic != null && topic.name().equals(data.properties.get(ProfileData.TOPIC))) return;
    data.properties.put(ProfileData.TOPIC, topic != null ? topic.name() : null);
    data.currentTopicStats = data.topicStats.get(topic != null ? topic.name() : topic);
    if (data.currentTopicStats == null && topic != null) {
      data.currentTopicStats = new HashMap<String, float[]>();
      data.topicStats.put(topic.name(), data.currentTopicStats);
    }
    save();
  }
  
  public Topic getCurrentTopic() {
    String s = data.properties.get(ProfileData.TOPIC);
    try {
      return s == null || s.length() == 0 ? null : Topic.valueOf(s);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  public String getUserName() {
    String s = data.properties.get(ProfileData.USER_NAME);
    return s == null ? "Guest" : s;
  }

  public String getUserEmail() {
    String s = data.properties.get(ProfileData.USER_ID);
    return s == null ? "" : s;
  }

  void testSetUserEmail(String userEmail) {
    data.properties.put(ProfileData.USER_ID, userEmail);
  }
  
  public void save() {
    data.properties.put(ProfileData.LAST_UPDATED, String.valueOf(System.currentTimeMillis()));
    ScienceEngine.getPreferencesManager().saveUserProfile();
  }
  
  public long getLastUpdated() {
    try {
      String lastUpdated = data.properties.get(ProfileData.LAST_UPDATED);
      return Long.parseLong(lastUpdated);
    } catch (IllegalArgumentException e) {
      return 0;
    }
  }
  
  public void addFriend(String friendEmail) {
    String[] currentFriends = getFriends();
    // We dont want duplicates - convert to set to eliminate duplicates.
    Set<String> friendSet = new HashSet<String>(Arrays.asList(currentFriends));
    friendSet.add(friendEmail.toLowerCase());
    data.social.friends = friendSet.toArray(new String[0]);
    save();
  }
  
  public String[] getFriends() {
    if (data.social == null) {
      data.social = new ProfileData.Social();
    }
    if (data.social.friends == null) {
      if (getUserEmail().length() == 0) return new String[0];
      String currentFriendStr = getUserEmail().toLowerCase();
      data.social.friends = new String[] { currentFriendStr };
      save();
    }
    return data.social.friends;
  }
  
  public void postMessage(String emailTo, int giftType, String text, String image, int points) {
    if (data.social.outbox == null) {
      data.social.outbox = new ArrayList<Message>();
    }
    Message msg = new Message(data.social.lastOutboxMessageId++, emailTo, giftType, text, image, points);
    data.social.outbox.add(msg);
    save();
  }
  
  void testPostInMessage(String emailFrom, int giftType, String text, String image, int points) {
    if (data.social.inbox == null) {
      data.social.inbox = new ArrayList<Message>();
    }
    Message msg = new Message(data.social.lastInboxMessageId++, emailFrom, giftType, text, image, points);
    data.social.inbox.add(msg);
  }
  
  public List<Message> getOutbox() {
    return data.social.outbox;
  }

  public List<Message> getInbox() {
    return data.social.inbox;
  }

  public void setCoachPixmap(Pixmap coachPixmap, String current, String color) {
    byte[] coachPngBytes = ScienceEngine.getPlatformAdapter().pixmap2Bytes(coachPixmap);
    Gdx.app.error(ScienceEngine.LOG, " bytes = " + coachPngBytes.length);
    data.properties.put(ProfileData.COACH_PNG, new String(Base64Coder.encode(coachPngBytes)));
    data.properties.put(ProfileData.CURRENT, current);
    data.properties.put(ProfileData.COLOR, color);
    save();
  }
  
  public Pixmap getCoachPixmap() {
    String png = data.properties.get(ProfileData.COACH_PNG);
    return png == null ? null : ScienceEngine.getPlatformAdapter().bytes2Pixmap(Base64Coder.decode(png));
  }

  public void setUserPixmap(Pixmap userPixmap) {
    byte[] userPngBytes = ScienceEngine.getPlatformAdapter().pixmap2Bytes(userPixmap);
    Gdx.app.error(ScienceEngine.LOG, " bytes = " + userPngBytes.length);
    data.properties.put(ProfileData.USER_PNG, new String(Base64Coder.encode(userPngBytes)));
    save();
  }
  
  public Pixmap getUserPixmap() {
    String png = data.properties.get(ProfileData.USER_PNG);
    return png == null ? null : ScienceEngine.getPlatformAdapter().bytes2Pixmap(Base64Coder.decode(png));
  }

  public float[] getStats(Topic topic, String tutorId) {
    Map<String, float[]> topicStat = data.topicStats.get(topic.name());
    float[] s = topicStat.get(tutorId);
    
    if (s == null) return new float[ITutor.NUM_STATS];
    
    if (s.length >= ITutor.NUM_STATS) return s;
    
    float[] stats = new float[ITutor.NUM_STATS];
    for (int i = 0; i < s.length; i++) {
      stats[i] = s[i];
    }
    return stats;
  }

  public float[] getStats(String tutorId) {
    return getStats(getCurrentTopic(), tutorId);
  }
  /**
   * Save stats for current topic, current activity.
   * @param stats
   * @param tutorId
   */
  public void saveStats(float[] stats, String tutorId) {
    data.currentTopicStats.put(tutorId, stats);
  }

  public void setPlatform(Platform platform) {
    data.properties.put(ProfileData.PLATFORM, platform.name());
  }

  public String getInstallationId() {
    return data.properties.get(ProfileData.INSTALL_ID);
  }

  public static Profile fromBase64(String profileBase64) {
    Profile profile = null;
    if (profileBase64 != null && profileBase64.length() > 0) {
      // decode the contents - base64 encoded
      String profileJson = Base64Coder.decodeString(profileBase64);
      try {
        profile = new Json().fromJson(Profile.class, profileJson);
      } catch (SerializationException s) {
        Gdx.app.error(ScienceEngine.LOG, "Error deserializing: " + s.getMessage() + "\n" + profileJson);
      } catch (IllegalArgumentException s) {
        Gdx.app.error(ScienceEngine.LOG, "Error deserializing: " + s.getMessage() + "\n" + profileJson);
      }
    }
    return profile;
  }

  public String toBase64() {
    String profileAsText = new Json(OutputType.json).toJson(this);
    Gdx.app.log(ScienceEngine.LOG, "Saving Profile - " + profileAsText);
    String profileAsBase64 = Base64Coder.encodeString(profileAsText);
    return profileAsBase64;
  }

  public synchronized void mergeProfile(String otherProfileBase64) {
    Profile otherProfile = fromBase64(otherProfileBase64);
    if (otherProfile != null) {
      // Other profile is later - merge other on top of this
      if (otherProfile.getLastUpdated() > getLastUpdated()) {
        data.properties.putAll(otherProfile.data.properties);
      } else {
        otherProfile.data.properties.putAll(data.properties);
        data.properties = otherProfile.data.properties;
      }
    }
  }
}
