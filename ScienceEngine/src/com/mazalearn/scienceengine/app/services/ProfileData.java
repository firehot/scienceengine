package com.mazalearn.scienceengine.app.services;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfileData {
  public static final String USER_EMAIL = "useremail";
  public static final String INSTALL_ID = "installid";
  public static final String USER_NAME = "username";
  public static final String USER_ID = "userid";
  public static final String LAST_UPDATED = "lastupdated";
  public static final String SEX = "sex";
  public static final String GRADE = "grade";
  public static final String SCHOOL = "school";
  public static final String CITY = "city";
  public static final String COMMENTS = "comments";
  public static final String COLOR = "color";
  public static final String CURRENT = "current";
  public static final String PNG = "png";
  
  // These are the units of synchronization since each is large.
  // TopicStats is a collection of changed activity stats - not atomic.
  public static final String SOCIAL = "social";
  public static final String CLIENT_PROPS = "client";
  public static final String SERVER_PROPS = "server";
  public static final String TOPIC_STATS = "topicStats";
  public static final String LAST_SYNC_TIME = "lastsynctime";
  public static final String COACH_PNG = "pngcoach";
  public static final String USER_PNG = "pnguser";
  public static final String THIS_SYNC_TIME = "thissynctime";

  public transient HashMap<String, float[]> currentActivityStats;

  public ClientProps client;
  public ServerProps server;
  public HashMap<String, HashMap<String, float[]>> topicStats;
  public ProfileData.Social social;
  public HashMap<String, Long> lastUpdated = new HashMap<String, Long>();
  public String userPng;
  public String coachPng;
  
  public static class ClientProps {
    public String userEmail;
    public String platform;
    public String color;
    public float current;
    public String installId;
    public String topic;
    public String lastActivity;
    public String activity;
  }
  public static class ServerProps {
    public String userName;
    public String userId;
    public String sex;
    public String grade;
    public String school;
    public String city;
    public String comments;
    public String registrationDate;
  }
  
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
    public ArrayList<String> friends;
    public ArrayList<Message> inbox; // server can only add, client can only remove 
    public ArrayList<Message> outbox; // client can only add, server can only remove
    public int lastInboxMessageId;
    public int lastOutboxMessageId;
    public int points;
    
    public Social() {
      inbox = new ArrayList<Message>();
      outbox = new ArrayList<Message>();
    }
  }

  public ProfileData() {
  }
}