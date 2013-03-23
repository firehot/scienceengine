package com.mazalearn.scienceengine.tutor;

import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.services.Profile;

public class TutorStats {
  public static final int NUM_ATTEMPTS = 0;
  public static final int NUM_SUCCESSES = 1;
  public static final int PERCENT_PROGRESS = 2;
  public static final int TIME_SPENT = 3;
  public static final int FAILURE_TRACKER = 4;
  private static final int NUM_STATS = 5;
  public float[] stats = new float[NUM_STATS];
  private Profile profile;
  private String tutorId;
  private Topic topic, level;
  
  public TutorStats(String tutorId) {
    this.profile = ScienceEngine.getPreferencesManager().getProfile();
    this.tutorId = tutorId;
    this.topic = profile.getCurrentTopic();
    this.level = profile.getCurrentActivity();
    profile.loadStats(this, topic, level, tutorId);
  }
  
  public TutorStats(Topic topic, Topic level, String tutorId) {
    this.profile = ScienceEngine.getPreferencesManager().getProfile();
    this.topic = topic;
    this.level = level;
    this.tutorId = tutorId;
    profile.loadStats(this, topic, level, tutorId);
  }

  public void save() {
    profile.saveStats(this, tutorId);
  }
  
  public String toString() {
    return "Topic: " + topic.name() +
        " Tutor: " + tutorId + 
        ", Time spent: " + stats[TIME_SPENT] + 
        ", NumAttempts: " + stats[NUM_ATTEMPTS] +
        ", numSuccesses: " + stats[NUM_SUCCESSES] +
        ", failureTracker: " + stats[FAILURE_TRACKER] + 
        ", percentProgress: " + stats[PERCENT_PROGRESS];
  }
}