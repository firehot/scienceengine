package com.mazalearn.scienceengine.app.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.mazalearn.scienceengine.DesktopPlatformAdapter;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.services.ProfileData.Social.Message;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;

/**
 * Test for profile.java
 * 
 */
public class ProfileTest {

  public static class TestProfile extends Profile {
    @Override
    public void save() {
      Writer writer = new StringWriter();
      new Json(OutputType.javascript).toJson(this, writer);
      System.out.println(writer.toString());
    }
  }

  private Profile profile;
  private static final String TEST_EMAIL = "test@mazalearn.com";
  private static final String TEST_EMAIL2 = "test2@mazalearn.com";
  
  @BeforeClass
  public static void setUp() {
    ScienceEngine.setPlatformAdapter(new DesktopPlatformAdapter(IPlatformAdapter.Platform.Desktop) {
      @Override
      public String getInstallationId() {
        return "TestInstallationId";
      }
    });
  }
  
  @AfterClass
  public static void tearDown() {
  }

  public ProfileTest() {
    profile = new TestProfile();
  }
  
  @Test
  public void testGetFriends_Unregistered() {
    List<String> currentFriends = profile.getFriends();
    assertEquals(0, currentFriends.size());
  }

  @Test
  public void testAddFriend_DuplicatesIgnored() {
    profile.addFriend(TEST_EMAIL);
    List<String> currentFriendsOld = profile.getFriends();
    
    profile.addFriend(TEST_EMAIL);
    List<String> currentFriendsNew = profile.getFriends();
    assertEquals(currentFriendsOld.size(), currentFriendsNew.size());
  }
  
  @Test
  public void testAddFriend() {
    profile.addFriend(TEST_EMAIL);
    profile.addFriend(TEST_EMAIL2);
    List<String> currentFriends = profile.getFriends();
    assertEquals(2, currentFriends.size());
    assertTrue(currentFriends.contains(TEST_EMAIL));
    assertTrue(currentFriends.contains(TEST_EMAIL2));
  }  

  @Test
  public void testPostMessage() {
    Message msg = new Message();
    msg.email = "test@mazalearn.com";
    msg.giftType = 1;
    msg.text = "text";
    msg.image = "image";
    msg.points = 500;
    profile.sendGift(msg);
    assertEquals(1, profile.getOutbox().size());
    profile.markForSync(ProfileData.SOCIAL);
    profile.save();
    Message msg2 = profile.getOutbox().get(0);
    assertEquals(1, msg2.giftType);
    assertEquals("text", msg2.text);
    assertEquals("image", msg2.image);
    assertEquals(500, msg2.points);
    assertEquals("test@mazalearn.com", msg2.email);
  }  

  @Test
  public void testGetSyncStr() throws IOException {
    profile.setCurrentTopic(Topic.Electromagnetism);
    profile.setCurrentActivity(Topic.BarMagnet);
    profile.saveStats(new float[]{1, 2, 3, 4, 5, 6}, "101$G1.1");
    profile.addFriend("test@mazalearn.com");
    System.out.println("Sync Str: " + profile.getSyncStr());
  }
  
  @Test
  public void testRead() {
    Profile profile = new Json(OutputType.javascript).fromJson(TestProfile.class, 
        "{ lastupdated: {client:28,test:10,social:38,BarMagnet:48}," +
        "client:{lastActivity:\"\",activity:\"BarMagnet\",topic:\"Electromagnetism\"," +
        "installId:\"TestInstallationId\"}," +
        "social:{friends:[\"test@mazalearn.com\"]}, " +
        "topicStats:{BarMagnet:{\"101$G1.1\":[1.0,2.0,3.0,4.0,5.0,6.0]}}}"
        );
    profile.save();
  }

//  @Test
  public void testWriteAfterRead() {
    Profile profile = new Json(OutputType.javascript).fromJson(TestProfile.class, 
        "{ lastupdated: {client:28,test:10,social:38,BarMagnet:48}," +
        "client:{lastActivity:\"\",activity:\"BarMagnet\",topic:\"Electromagnetism\"," +
        "installId:\"TestInstallationId\"}," +
        "social:{friends:[\"test@mazalearn.com\"]}, " +
        "topicStats:{BarMagnet:{\"101$G1.1\":[1.0,2.0,3.0,4.0,5.0,6.0]}}}"
        );
    profile.save();
    profile.write(new Json(OutputType.javascript));
  }

}
