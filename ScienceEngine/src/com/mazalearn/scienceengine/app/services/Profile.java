package com.mazalearn.scienceengine.app.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.mazalearn.scienceengine.app.services.ProfileData.ClientProps;
import com.mazalearn.scienceengine.app.services.ProfileData.ServerProps;
import com.mazalearn.scienceengine.app.services.ProfileData.Social.MQ;
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

  public static final String GUEST = "Guest";
  protected ProfileData data = new ProfileData();

  public Profile() {
    data.topicStats = new HashMap<String, HashMap<String, float[]>>();
    data.client = new ClientProps();
    data.server = new ServerProps();
    data.client.installId = ScienceEngine.getPlatformAdapter().getInstallationId();
    data.social = new ProfileData.Social();
  }

  public void setCurrentActivity(Topic level) {
    Topic activity = getCurrentActivity();
    if (level == activity) return;
    
    data.client.lastActivity = activity != null ? activity.name() : "";
    data.client.activity = level != null ? level.name() : "";
    data.currentActivityStats = level != null ? data.topicStats.get(level.name()) : null;
    if (data.currentActivityStats == null && level != null) {
      data.currentActivityStats = new HashMap<String, float[]>();
      data.topicStats.put(level.name(), data.currentActivityStats);
    }
    markForSync(ProfileData.CLIENT_PROPS);
    save();
  }
  
  /**
   * Retrieves the ID of the current level.
   */
  public Topic getCurrentActivity() {
    String levelStr = data.client.activity;
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
    String levelStr = data.client.lastActivity;
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

    data.lastUpdated = json.readValue(ProfileData.LAST_UPDATED, HashMap.class, Long.class, jsonData);
    data.serverTimestamps = json.readValue(ProfileData.SERVER_TIME_STAMPS, HashMap.class, Long.class, jsonData);
    data.client = json.readValue(ProfileData.CLIENT_PROPS, ClientProps.class, jsonData);
    if (data.client == null) {
      data.client = new ClientProps();
      data.client.installId = ScienceEngine.getPlatformAdapter().getInstallationId();
    }
    
    data.server = json.readValue(ProfileData.SERVER_PROPS, ServerProps.class, jsonData);
    if (data.server == null) {
      data.server = new ServerProps();
    }
    data.coachPng = json.readValue(ProfileData.COACH_PNG, String.class, jsonData);
    data.userPng = json.readValue(ProfileData.USER_PNG, String.class, jsonData);
    
    Object topicObj = json.readValue(ProfileData.TOPIC_STATS, OrderedMap.class, OrderedMap.class, jsonData);
    data.topicStats = new HashMap<String, HashMap<String, float[]>>();
    if (topicObj != null) {
      for (Topic topic: Topic.values()) {
        HashMap<String, float[]> stats = json.readValue(topic.name(), HashMap.class, float[].class, topicObj);
        if (stats != null) {
          data.topicStats.put(topic.name(), stats);
        }
      }
    }
    data.social = json.readValue(ProfileData.SOCIAL, ProfileData.Social.class, jsonData);
    
    // Set current activity
    Topic currentActivity = Topic.BarMagnet;
    try {
      currentActivity = Topic.valueOf(data.client.activity);
    } catch (Exception ignored) {}
    
    data.currentActivityStats = data.topicStats.get(currentActivity.name());
    if (data.currentActivityStats == null) {
      data.currentActivityStats = new HashMap<String, float[]>();
      data.topicStats.put(currentActivity.name(), data.currentActivityStats);
    }
  }

  @Override
  public void write(Json json) {
    json.writeValue(ProfileData.LAST_UPDATED, data.lastUpdated, HashMap.class, Long.class);
    json.writeValue(ProfileData.SERVER_TIME_STAMPS, data.serverTimestamps, HashMap.class, Long.class);
    json.writeValue(ProfileData.CLIENT_PROPS, data.client);
    json.writeValue(ProfileData.SERVER_PROPS, data.server);
    if (data.coachPng != null) {
      json.writeValue(ProfileData.COACH_PNG, data.coachPng);
    }
    if (data.userPng != null) { 
      json.writeValue(ProfileData.USER_PNG, data.userPng);
    }
    json.writeObjectStart(ProfileData.TOPIC_STATS);
    for (Topic topic: Topic.values()) {
      Map<String,?> props = data.topicStats.get(topic.name());
      if (props != null) {
        json.writeValue(topic.name(), props);
      }
    }
    json.writeObjectEnd();
    json.writeValue(ProfileData.SOCIAL, data.social);
  }

  public void setCurrentTopic(Topic topic) {
    if (topic != null && topic.name().equals(data.client.topic)) return;
    data.client.topic = topic != null ? topic.name() : null;
    markForSync(ProfileData.CLIENT_PROPS);
    save();
  }
  
  public Topic getCurrentTopic() {
    String s = data.client.topic;
    try {
      return s == null || s.length() == 0 ? null : Topic.valueOf(s);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  public String getUserName() {
    String s = data.server.userName;
    return s == null ? GUEST : s;
  }

  public String getUserEmail() {
    String s = data.server.userId;
    return s == null ? "" : s;
  }

  void testSetUserEmail(String userEmail) {
    data.server.userId = userEmail;
  }
  
  public void save() {
    ScienceEngine.getPreferencesManager().saveUserProfile();
  }
  
  public void addFriend(String friendEmail) {
    List<String> currentFriends = getFriends();
    // We dont want duplicates
    if (currentFriends.indexOf(friendEmail.toLowerCase()) == -1) {
      data.social.friends.add(friendEmail.toLowerCase());
      markForSync(ProfileData.SOCIAL);
      save();
    }
  }
  
  public List<String> getFriends() {
    if (data.social == null) {
      data.social = new ProfileData.Social();
    }
    if (data.social.friends == null) {
      data.social.friends = new ArrayList<String>();
    }
    return data.social.friends;
  }
  
  public void sendGift(Message gift) {
    if (data.social.outbox == null) {
      data.social.outbox = new MQ();
    }
    data.social.outbox.addMessage(gift);
    data.social.points -= gift.points;
    markForSync(ProfileData.SOCIAL);
    save();
  }

  void testPostInMessage(Message msg) {
    if (data.social.inbox == null) {
      data.social.inbox = new MQ();
    }
    data.social.inbox.addMessage(msg);
  }
  
  public List<Message> getOutbox() {
    return data.social.outbox.mq;
  }

  public List<Message> getInbox() {
    return data.social.inbox.mq;
  }

  public void acceptGift(Message gift) {
    data.social.inbox.mq.remove(gift);
    data.social.points += gift.points;
    markForSync(ProfileData.SOCIAL);
    save();
  }

  public void setCoachPixmap(Pixmap coachPixmap, float current, String color) {
    byte[] coachPngBytes = ScienceEngine.getPlatformAdapter().pixmap2Bytes(coachPixmap);
    Gdx.app.error(ScienceEngine.LOG, " bytes = " + coachPngBytes.length);
    data.coachPng = new String(Base64Coder.encode(coachPngBytes));
    data.client.current = current;
    data.client.color = color;
    markForSync(ProfileData.COACH_PNG);
    save();
  }
  
  public Pixmap getCoachPixmap() {
    String png = data.coachPng;
    return png == null ? null : ScienceEngine.getPlatformAdapter().bytes2Pixmap(Base64Coder.decode(png));
  }

  public void setUserPixmap(Pixmap userPixmap) {
    byte[] userPngBytes = ScienceEngine.getPlatformAdapter().pixmap2Bytes(userPixmap);
    Gdx.app.error(ScienceEngine.LOG, " bytes = " + userPngBytes.length);
    data.userPng = new String(Base64Coder.encode(userPngBytes));
    markForSync(ProfileData.USER_PNG);
    save();
  }
  
  public Pixmap getUserPixmap() {
    String png = data.userPng;
    return png == null ? null : ScienceEngine.getPlatformAdapter().bytes2Pixmap(Base64Coder.decode(png));
  }

  public float[] getStats(Topic topic, String tutorId) {
    Map<String, float[]> topicStat = data.topicStats.get(topic.name());
    if (topicStat == null) return new float[ITutor.NUM_STATS];
    
    float[] s = topicStat.get(tutorId);
    
    if (s == null) return new float[ITutor.NUM_STATS];
    
    if (s.length >= ITutor.NUM_STATS) return s;
    
    float[] stats = new float[ITutor.NUM_STATS];
    for (int i = 0; i < s.length; i++) {
      stats[i] = s[i];
    }
    return stats;
  }

  /**
   * Save stats for current topic, current activity.
   * @param stats
   * @param tutorId
   */
  public void saveStats(float[] stats, String tutorId) {
    data.currentActivityStats.put(tutorId, stats);
    markForSync(data.client.activity);
  }
  
  public void markForSync(String key) {
    data.lastUpdated.put(key, System.currentTimeMillis());    
  }

  public void setPlatform(Platform platform) {
    data.client.platform = platform.name();
  }

  public String getInstallationId() {
    return data.client.installId;
  }

  public static Profile fromBase64(String profileBase64) {
    Profile profile = null;
    if (profileBase64 != null && profileBase64.length() > 0) {
      // decode the contents - base64 encoded
      String profileJson = Base64Coder.decodeString(profileBase64);
      try {
        profile = new Json(OutputType.javascript).fromJson(Profile.class, profileJson);
      } catch (SerializationException s) {
        Gdx.app.error(ScienceEngine.LOG, "Error deserializing: " + s.getMessage() + "\n" + profileJson);
      } catch (IllegalArgumentException s) {
        Gdx.app.error(ScienceEngine.LOG, "Error deserializing: " + s.getMessage() + "\n" + profileJson);
      }
    }
    return profile;
  }

  public String toBase64() {
    String profileAsText = new Json(OutputType.javascript).toJson(this);
    Gdx.app.log(ScienceEngine.LOG, "Saving Profile - " + profileAsText);
    String profileAsBase64 = Base64Coder.encodeString(profileAsText);
    return profileAsBase64;
  }

  /*
  private static <T> T merge(T sItem, T cItem, Map<String, Long> sUpdate, Map<String, Long> cUpdate, String key) {
    long serverLastUpdated = nvl(sUpdate.get(key), 0);
    long clientLastUpdated = nvl(cUpdate.get(key), 0);
    return ( serverLastUpdated > clientLastUpdated) ? sItem : cItem;
  } */
  
  public synchronized void mergeProfile(String serverProfileBase64) {
    Profile serverProfile = fromBase64(serverProfileBase64);
    // TODO: add locking - getProfileData() should lock on profiledata
    // Not locking ProfileData here - so chances of overwrites.
    if (serverProfile != null) {
      new ProfileSyncer().mergeProfile(serverProfile.data, data);
    }
  }

  public int getPoints() {
    // TODO Auto-generated method stub
    return 10000;
  }

  public int getGiftPoints() {
    return data.social.points;
  }

  // Returns the profile string to be used for syncing to server
  public String getSyncStr() {
    Map<String, Object> syncData = new ProfileSyncer().getSyncJson(data);
    String syncProfileStr = new Json(OutputType.javascript).toJson(syncData);
    Gdx.app.log(ScienceEngine.LOG, syncProfileStr);
    return Base64Coder.encodeString(syncProfileStr);
    
  }

  public boolean isRegistered() {
    return data.server.isRegistered;
  }

  public List<String> getCertificates() {
    return data.client.certificates;
  }
  
  public void addCertificate(String certificate) {
    if (!data.client.certificates.contains(certificate)) {
      data.client.certificates.add(certificate);
      markForSync(ProfileData.CLIENT_PROPS);
    }
  }
}
