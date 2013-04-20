package com.mazalearn.scienceengine.app.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.mazalearn.scienceengine.PlatformAdapterImpl;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.Profile.Social.Message;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;

/**
 * Test for profile.java
 * 
 */
public class ProfileTest {

  private Profile profile;
  private static final String TEST_EMAIL = "test@mazalearn.com";
  private static final String TEST_EMAIL2 = "test2@mazalearn.com";
  
  @BeforeClass
  public static void setUp() {
    ScienceEngine.setPlatformAdapter(new PlatformAdapterImpl(IPlatformAdapter.Platform.Desktop) {
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
    profile = new Profile() {
      @Override
      public void save() {
        Writer writer = new StringWriter();
        new Json(OutputType.javascript).toJson(this, writer);
        System.out.println(writer.toString());
      }      
    };
  }
  
  @Test
  public void testGetFriends_Unregistered() {
    String[] currentFriends = profile.getFriends();
    assertEquals(0, currentFriends.length);
  }

  @Test
  public void testGetFriends_Empty() {
    profile.testSetUserEmail(TEST_EMAIL);
    String[] currentFriends = profile.getFriends();
    assertEquals(1, currentFriends.length);
    assertEquals(TEST_EMAIL, currentFriends[0]);
  }

  @Test
  public void testAddFriend_DuplicatesIgnored() {
    profile.testSetUserEmail(TEST_EMAIL);
    profile.addFriend(TEST_EMAIL);
    String[] currentFriendsOld = profile.getFriends();
    
    profile.addFriend(TEST_EMAIL);
    String[] currentFriendsNew = profile.getFriends();
    assertEquals(currentFriendsOld.length, currentFriendsNew.length);
  }
  
  @Test
  public void testAddFriend() {
    profile.testSetUserEmail(TEST_EMAIL);
    profile.addFriend(TEST_EMAIL2);
    String[] currentFriends = profile.getFriends();
    assertEquals(2, currentFriends.length);
    List<String> friendsList = Arrays.asList(currentFriends);
    assertTrue(friendsList.contains(TEST_EMAIL));
    assertTrue(friendsList.contains(TEST_EMAIL2));
  }  

  @Test
  public void testPostMessage() {
    profile.postMessage("test@mazalearn.com", 1, "text", "image", 500);
    assertEquals(1, profile.getOutbox().size());
    profile.save();
    Message msg = profile.getOutbox().get(0);
    assertEquals(1, msg.giftType);
    assertEquals("text", msg.text);
    assertEquals("image", msg.image);
    assertEquals(500, msg.points);
    assertEquals("test@mazalearn.com", msg.email);
  }  
}
