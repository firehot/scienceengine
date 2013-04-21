package com.mazalearn.scienceengine.app.services;

import java.util.ArrayList;
import java.util.Map;

public class ProfileData {
  // Client owns below data items and can read and write. Server can only read.
  public static final String USER_EMAIL = "useremail";
  public static final String PLATFORM = "platform";
  public static final String COLOR = "color";
  public static final String CURRENT = "current";
  public static final String INSTALL_ID = "installid";
  public static final String TOPIC = "topic";
  public static final String LAST_ACTIVITY = "last_activity";
  public static final String ACTIVITY = "activity";
  public static final String PNG = "png";
  public static final String USER_PNG = PNG + "user";
  public static final String COACH_PNG = PNG + "coach";
  // Server owns below data items and can read and write. Client can only read.
  public static final String USER_NAME = "username";
  public static final String USER_ID = "userid";
  public static final String SEX = "sex";
  public static final String GRADE = "grade";
  public static final String SCHOOL = "school";
  public static final String CITY = "city";
  public static final String COMMENTS = "comments";
  public static final String REGN_DATE = "regndate";
  // Changed by both server and client
  public static final String LAST_UPDATED = "last_updated";
  public static final String SOCIAL = "social";

  public Map<String, Map<String, float[]>> topicStats;
  public Map<String, String> properties;
  public transient Map<String, float[]> currentTopicStats;
  public ProfileData.Social social;

  public static class Social {
    public static class Message {
      // Following params should all be set only once
      public int messageId;
      public String email;
      public int giftType;
      public String text;
      public String image;
      public int points;
      public int status; // 0 = not processed, 1 = sent, 2 = problem
      public Message() {}
      public Message(int messageId, Message other) {
        this.messageId = messageId;
        this.email = other.email;
        this.giftType = other.giftType;
        this.text = other.text;
        this.image = other.image;
        this.points = other.points;
      }
    }
    String[] friends;
    ArrayList<Message> inbox; // owned by server
    ArrayList<Message> outbox; // owned by client except for status in messages
    int lastInboxMessageId;
    int lastOutboxMessageId;
    
    public Social() {
      inbox = new ArrayList<Message>();
      outbox = new ArrayList<Message>();
    }
  }

  public ProfileData() {
  }
}