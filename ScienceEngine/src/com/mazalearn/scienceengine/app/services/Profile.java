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
import com.mazalearn.scienceengine.app.services.ProfileData.ClientProps;
import com.mazalearn.scienceengine.app.services.ProfileData.ServerProps;
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
  private ProfileData data = new ProfileData();

  public Profile() {
    data.topicStats = new HashMap<String, Map<String, float[]>>();
    for (Topic topic: Topic.values()) {
      if (topic.getChildren().length == 0) continue;
      data.topicStats.put(topic.name(), new HashMap<String, float[]>());
    }
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

    data.client = json.readValue("client", ClientProps.class, jsonData);
    if (data.client == null) {
      data.client = new ClientProps();
      data.client.installId = ScienceEngine.getPlatformAdapter().getInstallationId();
    }
    
    data.server = json.readValue("server", ServerProps.class, jsonData);
    if (data.server == null) {
      data.server = new ServerProps();
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
      currentTopic = Topic.valueOf(data.client.topic);
    } catch (Exception ignored) {}
    data.currentTopicStats = data.topicStats.get(currentTopic.name());
  }

  @Override
  public void write(Json json) {
    json.writeValue("client", data.client);
    // No need to send server values
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
    if (topic != null && topic.name().equals(data.client.topic)) return;
    data.client.topic = topic != null ? topic.name() : null;
    data.currentTopicStats = data.topicStats.get(topic != null ? topic.name() : topic);
    if (data.currentTopicStats == null && topic != null) {
      data.currentTopicStats = new HashMap<String, float[]>();
      data.topicStats.put(topic.name(), data.currentTopicStats);
    }
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
    data.client.lastUpdated = System.currentTimeMillis();
    ScienceEngine.getPreferencesManager().saveUserProfile();
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
  
  public void sendGift(Message gift) {
    if (data.social.outbox == null) {
      data.social.outbox = new ArrayList<Message>();
    }
    Message msg = new Message(data.social.lastOutboxMessageId++, gift);
    data.social.outbox.add(msg);
    data.social.points -= gift.points;
    save();
  }
  
  void testPostInMessage(Message msg) {
    if (data.social.inbox == null) {
      data.social.inbox = new ArrayList<Message>();
    }
    Message message = new Message(data.social.lastInboxMessageId++, msg);
    data.social.inbox.add(message);
  }
  
  public List<Message> getOutbox() {
    return data.social.outbox;
  }

  public List<Message> getInbox() {
    return data.social.inbox;
  }

  public void setCoachPixmap(Pixmap coachPixmap, float current, String color) {
    byte[] coachPngBytes = ScienceEngine.getPlatformAdapter().pixmap2Bytes(coachPixmap);
    Gdx.app.error(ScienceEngine.LOG, " bytes = " + coachPngBytes.length);
    data.client.pngCoach = new String(Base64Coder.encode(coachPngBytes));
    data.client.current = current;
    data.client.color = color;
    save();
  }
  
  public Pixmap getCoachPixmap() {
    String png = data.client.pngCoach;
    return png == null ? null : ScienceEngine.getPlatformAdapter().bytes2Pixmap(Base64Coder.decode(png));
  }

  public void setUserPixmap(Pixmap userPixmap) {
    byte[] userPngBytes = ScienceEngine.getPlatformAdapter().pixmap2Bytes(userPixmap);
    Gdx.app.error(ScienceEngine.LOG, " bytes = " + userPngBytes.length);
    data.client.pngUser = new String(Base64Coder.encode(userPngBytes));
    save();
  }
  
  public Pixmap getUserPixmap() {
    String png = data.client.pngUser;
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

  public synchronized void mergeProfile(String serverProfileBase64) {
    Profile serverProfile = fromBase64(serverProfileBase64);
    if (serverProfile != null) {
      // Other profile is later - merge other on top of this
      if (serverProfile.data.client.lastUpdated > data.client.lastUpdated) {
        data.client = serverProfile.data.client;
      }
      if (serverProfile.data.server != null) {
        data.server = serverProfile.data.server;
      }
      // Get inbox messages from server into local inbox
      for (Message msg: serverProfile.data.social.inbox) {
        if (msg.messageId < data.social.lastInboxMessageId) continue;
        data.social.inbox.add(msg);
        data.social.lastInboxMessageId = Math.max(data.social.lastInboxMessageId, msg.messageId);
      }
    }
  }

  public void acceptGift(Message gift) {
    data.social.inbox.remove(gift);
    data.social.points += gift.points;
  }

  public int getPoints() {
    // TODO Auto-generated method stub
    return 10000;
  }

  public int getGiftPoints() {
    return data.social.points;
  }
}
