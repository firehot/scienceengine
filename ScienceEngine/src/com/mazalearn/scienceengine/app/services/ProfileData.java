package com.mazalearn.scienceengine.app.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The data component of Profile and methods shared between client and server
 * @author sridhar
 *
 */
public class ProfileData {
  public static final String USER_EMAIL = "useremail";
  public static final String INSTALL_ID = "installid";
  public static final String USER_NAME = "username";
  public static final String USER_ID = "userid";
  public static final String LAST_UPDATED = "lastUpdated";
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
  public static final String SERVER_TIME_STAMPS = "servertimestamps";

  public transient HashMap<String, float[]> currentActivityStats;

  public ClientProps client;
  public ServerProps server;
  public HashMap<String, HashMap<String, float[]>> topicStats;
  public ProfileData.Social social;
  // update timestamps for all synchronizable chunks of profile
  public HashMap<String, Long> lastUpdated = new HashMap<String, Long>();
  // base 64 encoded string of image of user face
  public String userPng;
  // base64 encoded string of png image of coach in science train
  public String coachPng;
  // Server timestamps received at client - not used at server
  public transient Map<String, Long> serverTimestamps;
  
  public static class ClientProps {
    // email id of user - not available until registration
    public String userEmail;
    // platform of current device of user
    public String platform;
    // value of color for Science Train
    public String color;
    // value of current for Science Train
    public float current;
    // Installation id of current device of user
    public String installId;
    // Current topic of user
    public String topic;
    // Previous to current activity of user
    public String lastActivity;
    // Current activity of user
    public String activity;
    // These are the set of certificates held by user
    public ArrayList<String> certificates = new ArrayList<String>();
    public ArrayList<Long> certificateTimes = new ArrayList<Long>();
    // Points earned through challenges and rapidfire
    public int points;
    // Rating related information
    public boolean dontShowAgain;
    public int launchCount;
    public long dateFirstLaunch;
    public boolean soundEnabled = true;
    public boolean musicEnabled = true;
    public boolean speechEnabled = true;
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
    public boolean isRegistered;
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
    
    // Message Queue shared between producer and consumer
    // Producer writes to tail of mq and consumer reads from head of mq
    // tailId belongs to producer and headId belongs to consumer
    // only producer is allowed to remove messages
    public static class MQ {
      public ArrayList<Message> mq = new ArrayList<Message>();
      public int tailId;
      public int headId;
      
      public void addMessage(Message gift) {
        Message msg = new Message(++tailId, gift);
        mq.add(msg);
      }
      
    }
    public ArrayList<String> friends = new ArrayList<String>();
    public MQ inbox;  // producer = server, consumer = client 
    public MQ outbox; // producer = client, consumer = server
    public int points; // gift points received
    
    public Social() {
      inbox = new MQ();
      outbox = new MQ();
    }
  }

  public ProfileData() {
  }
}