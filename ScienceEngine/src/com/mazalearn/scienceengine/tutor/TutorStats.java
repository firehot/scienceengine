package com.mazalearn.scienceengine.tutor;

import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.services.Profile;

public class TutorStats {
  public float timeSpent;
  public float numAttempts;
  public float numSuccesses;
  public float failureTracker;
  public float percentAttempted;
  private Profile profile;
  private String tutorId;
  private int level;
  private Topic topic;
  
  public TutorStats(String tutorId) {
    this.profile = ScienceEngine.getPreferencesManager().getProfile();
    this.tutorId = tutorId;
    this.level = profile.getCurrentActivity();
    this.topic = profile.getCurrentTopic();
    profile.loadStats(this, topic, level, tutorId);
  }
  
  public TutorStats(Topic topic, int level, String tutorId) {
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
        " Level: " + level + 
        " Tutor: " + tutorId + 
        ", Time spent: " + timeSpent + 
        ", NumAttempts: " + numAttempts +
        ", numSuccesses: " + numSuccesses +
        ", failureTracker: " + failureTracker + 
        ", percentAttempted: " + percentAttempted;
  }
}