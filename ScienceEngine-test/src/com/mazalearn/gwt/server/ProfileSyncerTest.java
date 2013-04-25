package com.mazalearn.gwt.server;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.mazalearn.scienceengine.app.services.ProfileData;
import com.mazalearn.scienceengine.app.services.ProfileData.ClientProps;
import com.mazalearn.scienceengine.app.services.ProfileData.ServerProps;
import com.mazalearn.scienceengine.app.services.ProfileData.Social;

/**
 * Test for profileutil.java
 * 
 */

public class ProfileSyncerTest {

  public static final long TIME_NOW = 123456L;


  private EmbeddedEntity serverUpdates = new EmbeddedEntity();
  private EmbeddedEntity serverProfile = new EmbeddedEntity();
  private HashMap<String, Long> clientUpdates = new HashMap<String, Long>();
  private ProfileData clientProfile = new ProfileData();
  private ProfileUtil profileUtil = new ProfileUtil() {
    @Override
    long getCurrentTime() {
      return TIME_NOW;
    }
  };

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
    serverUpdates.setProperty(ProfileData.TOPIC_STATS, 40L);
    serverUpdates.setProperty("Field", 40L);
    serverUpdates.setProperty("BarMagnet", 30L);

    serverProfile.setProperty(ProfileData.LAST_UPDATED, serverUpdates);
    serverProfile.setProperty(ProfileData.CLIENT_PROPS, new Text("{clientjson:test}"));
    serverProfile.setProperty(ProfileData.SERVER_PROPS, new Text("{serverjson:test}"));
    serverProfile.setProperty(ProfileData.SOCIAL,  new Text("{socialjson:test}"));
    EmbeddedEntity topicStats = new EmbeddedEntity();
    topicStats.setProperty("BarMagnet", new Text("{Guru: 10}"));
    topicStats.setProperty("Field", new Text("{FIeld: 20}"));
    serverProfile.setProperty(ProfileData.TOPIC_STATS,  topicStats);
    
    clientUpdates.put(ProfileData.CLIENT_PROPS, 10L);
    clientUpdates.put(ProfileData.SERVER_PROPS, 20L);
    clientUpdates.put(ProfileData.SOCIAL, 30L);
    
    clientUpdates.put(ProfileData.LAST_SYNC_TIME, 40L);
    clientUpdates.put(ProfileData.THIS_SYNC_TIME, 50L);
    clientProfile.lastUpdated = clientUpdates;
    clientProfile.client = new ClientProps();
    clientProfile.server = new ServerProps();
    clientProfile.social = null;
  }
  
  private static final String TIMESTAMPS_JSON =  "{\"lastupdated\":{}}";
  @Test
  public void testGetUserSyncProfile_NoUpdates() {
    String expected = "{\"lastupdated\":{}}";
    // Client is up to date as per setup
    String s = profileUtil.getUserSyncProfile(null, serverProfile, clientProfile);
    assertEquals(expected, s);
  }

  @Test
  public void testGetUserSyncProfile_YouBehindOnClient() {
    String expected= "{\"client\":{\"clientjson\":\"test\"}," + "\"lastupdated\":{\"client\":10}}";
    // clientTime < serverTime
    clientUpdates.put(ProfileData.CLIENT_PROPS, 5L);
    String s = profileUtil.getUserSyncProfile(null, serverProfile, clientProfile);
    assertEquals(expected, s);
    
    // clientTime not present - defaults to 0
    clientUpdates.remove(ProfileData.CLIENT_PROPS);
    s = profileUtil.getUserSyncProfile(null, serverProfile, clientProfile);
    assertEquals(expected, s); 
  }
  
  @Test
  public void testGetUserSyncProfile_MeBehindOnClient() {
    String expected= "{\"lastupdated\":{}}";
    // serverTime defaults to 0
    serverUpdates.removeProperty(ProfileData.CLIENT_PROPS);
    String s = profileUtil.getUserSyncProfile(null, serverProfile, clientProfile);
    assertEquals(expected, s);
  }

  @Test
  public void testGetUserSyncProfile_ReInitialize() {
    String expected= "{\"client\":{\"clientjson\":\"test\"},\"social\":{\"socialjson\":\"test\"},\"lastupdated\":{\"Field\":40,\"client\":10,\"BarMagnet\":30,\"social\":30,\"server\":20},\"server\":{\"serverjson\":\"test\"},\"topicStats\":{\"Field\":{\"FIeld\":20},\"BarMagnet\":{\"Guru\":10}}}";
    clientUpdates.clear();
    String s = profileUtil.getUserSyncProfile(null, serverProfile, clientProfile);
    assertEquals(expected, s);
  }

  @Test
  public void testGetUserSyncProfile_TopicStatsForced() {
    String expected= "{\"lastupdated\":{\"Field\":40,\"BarMagnet\":30},\"topicStats\":{\"Field\":{\"FIeld\":20},\"BarMagnet\":{\"Guru\":10}}}";
    clientUpdates.put(ProfileData.TOPIC_STATS, 0L);
    String s = profileUtil.getUserSyncProfile(null, serverProfile, clientProfile);
    assertEquals(expected, s);
  }
}
