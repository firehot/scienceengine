package com.mazalearn.scienceengine.tutor;

import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.services.Profile;

public class TutorStats {
  public float timeSpent;
  public float numAttempts;
  public float numSuccesses;
  public float failureTracker;
  public float percentProgress;
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
        ", Time spent: " + timeSpent + 
        ", NumAttempts: " + numAttempts +
        ", numSuccesses: " + numSuccesses +
        ", failureTracker: " + failureTracker + 
        ", percentProgress: " + percentProgress;
  }
}