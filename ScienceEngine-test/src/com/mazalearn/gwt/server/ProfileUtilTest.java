package com.mazalearn.gwt.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PropertyContainer;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import com.mazalearn.scienceengine.PlatformAdapterImpl;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.ProfileData;
import com.mazalearn.scienceengine.app.services.ProfileData.ServerProps;
import com.mazalearn.scienceengine.app.services.ProfileData.Social;
import com.mazalearn.scienceengine.app.services.ProfileData.Social.Message;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;

/**
 * Test for profileutil.java
 * 
 */

public class ProfileUtilTest {

  private EmbeddedEntity serverUpdates = new EmbeddedEntity();
  private EmbeddedEntity serverProfile = new EmbeddedEntity();
  private HashMap<String, Long> clientUpdates = new HashMap<String, Long>();
  private ProfileData clientProfile = new ProfileData();
  private ProfileUtil profileUtil = new ProfileUtil();

  @BeforeClass
  public static void staticSetUp() {
  }
  
  @AfterClass
  public static void staticTearDown() {
  }

  @Before
  public void setUp() {
    serverUpdates.setProperty(ProfileData.CLIENT_PROPS, 10L);
    serverUpdates.setProperty(ProfileData.SERVER_PROPS, 20L);
    serverUpdates.setProperty(ProfileData.SOCIAL, 30L);
    serverUpdates.setProperty(ProfileData.LAST_UPDATED, 40L);
    serverUpdates.setProperty(ProfileData.TOPIC_STATS, 50L);

    serverProfile.setProperty(ProfileData.LAST_UPDATED, serverUpdates);
    serverProfile.setProperty(ProfileData.CLIENT_PROPS, new Text("{}"));
    serverProfile.setProperty(ProfileData.SERVER_PROPS, new Text("{}"));
    serverProfile.setProperty(ProfileData.SOCIAL,  new Text("{}"));
    EmbeddedEntity topicStats = new EmbeddedEntity();
    topicStats.setProperty("BarMagnet", new Text("{Guru: 10}"));
    serverProfile.setProperty(ProfileData.TOPIC_STATS,  topicStats);
    
    clientUpdates.put(ProfileData.CLIENT_PROPS, 10L);
    clientUpdates.put(ProfileData.SERVER_PROPS, 20L);
    clientUpdates.put(ProfileData.SOCIAL, 30L);
    clientUpdates.put(ProfileData.LAST_UPDATED, 40L);
    clientUpdates.put(ProfileData.TOPIC_STATS, 50L);
    clientProfile.lastUpdated = clientUpdates;
  }
  
  // clientprofile may be null for a GET request
  String getUserSyncProfile(EmbeddedEntity serverProfile, ProfileData clientProfile) 
      throws IllegalStateException {
    System.out.println("getUserSyncProfile serverprofile: " + serverProfile);
    EmbeddedEntity serverUpdates = (EmbeddedEntity) serverProfile.getProperty(ProfileData.LAST_UPDATED);
    Map<String, Long> clientUpdates = 
        clientProfile != null ? clientProfile.lastUpdated : new HashMap<String, Long>();
    long clientDefault = nvl(clientUpdates.get(ProfileData.LAST_SYNC_TIME), 0L);
    StringBuilder json = new StringBuilder("{");
    String topicDelimiter = "";
    for (Map.Entry<String, Object> property: serverProfile.getProperties().entrySet()) {
      String key = property.getKey();
      Object value = property.getValue();
      // If server version is older than client version, no need to send
      if (nvl((Long)serverUpdates.getProperty(key), 1) <= nvl(clientUpdates.get(key), clientDefault)) continue;
      if (value instanceof Text) {
        String s = (value == null) ? "{}" : ((Text) value).getValue();
        if (!"null".equals(s)) {
          json.append(topicDelimiter + key + ":" + s);
          topicDelimiter = ",";
        }
      } else if (value instanceof EmbeddedEntity) {
        EmbeddedEntity embeddedEntity = (EmbeddedEntity) value;
        json.append(topicDelimiter + key + ":{");
        topicDelimiter = ",";
        String delimiter = "";
        for (Map.Entry<String, Object> p: embeddedEntity.getProperties().entrySet()) {
          Object v = p.getValue();
          // If server version is older than client version, no need to send
          if (v instanceof Text) {
            if (nvl((Long)serverUpdates.getProperty(p.getKey()), 1) <= nvl(clientUpdates.get(p.getKey()), clientDefault)) continue;
            String s = (v == null) ? "{}" : ((Text) v).getValue();
            if (!"null".equals(s)) {
              json.append(delimiter + p.getKey() + ":" + s);
              delimiter = ",";
            }
          } else if (v instanceof Long) {
            json.append(delimiter + p.getKey() + ":" + v);
            delimiter = ",";
          }
        }
        json.append("}");
      }
    }
    
    json.append("}");
    System.out.println("getUserSyncProfile: " + json);
    return json.toString();
  }

  private static long nvl(Long value, long defaultValue) {
    return value == null ? defaultValue : value;
  }

  @Test
  public void testGetUserSyncProfile_OnlyClient() {
    // Client is up to date as per setup
    String s = this.getUserSyncProfile(serverProfile, clientProfile);
    assertEquals("{}", s);
    // clientTime < serverTime
    clientUpdates.put(ProfileData.CLIENT_PROPS, 5L);
    s = this.getUserSyncProfile(serverProfile, clientProfile);
    assertEquals("{client:{}}", s);
    // clientTime > serverTime
    serverUpdates.setProperty(ProfileData.CLIENT_PROPS, 1L);
    s = this.getUserSyncProfile(serverProfile, clientProfile);
    assertEquals("{}", s);
    // clientTime defaults to 0 which is less than server time
    clientUpdates.remove(ProfileData.CLIENT_PROPS);
    s = this.getUserSyncProfile(serverProfile, clientProfile);
    assertEquals("{client:{}}", s);
    // clientTime defaults to 0 and serverTime defaults to 1
    serverUpdates.removeProperty(ProfileData.CLIENT_PROPS);
    s = this.getUserSyncProfile(serverProfile, clientProfile);
    assertEquals("{client:{}}", s);
    // clientTime defaults to last sync time, serverTime defaults to 1
    clientUpdates.put(ProfileData.LAST_SYNC_TIME, 10L);
    s = this.getUserSyncProfile(serverProfile, clientProfile);
    assertEquals("{}", s);
  }

  @Test
  public void testGetUserSyncProfile_LastUpdated() {
    clientUpdates.remove(ProfileData.LAST_UPDATED);
    String s = this.getUserSyncProfile(serverProfile, clientProfile);
    assertEquals("{lastupdated:{server:20,lastupdated:40,client:10,topicStats:50,social:30}}", s);
  }

  @Test
  public void testGetUserSyncProfile_TopicStats() {
    clientUpdates.remove(ProfileData.TOPIC_STATS);
    String s = this.getUserSyncProfile(serverProfile, clientProfile);
    assertEquals("{topicStats:{BarMagnet:{Guru: 10}}}", s);
  }

  @Test
  public void testGetUserSyncProfile_LastSyncTime() {
    clientUpdates.put(ProfileData.LAST_SYNC_TIME, 10L);
    String s = this.getUserSyncProfile(serverProfile, clientProfile);
    assertEquals("{topicStats:{BarMagnet:{Guru: 10}}}", s);
  }

}
