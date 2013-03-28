package com.mazalearn.scienceengine.tutor;

import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.services.Profile;

// TODO: use tutorstats in html jsp also.
public class TutorStats {
  
  public static final int NUM_ATTEMPTS = 0;
  public static final int NUM_SUCCESSES = 1;
  /**
   * Success percent on this tutor.
   * For a non-group tutor, this is 0 or 100.
   * For a group tutor, this is the percentage of children attempted successfully.
   */
  public static final int PERCENT_PROGRESS = 2;
  public static final int TIME_SPENT = 3;
  /**
   * Number tracking the failures and type of failures.
   * Failure is expressed as XXXXXX 
   * Each X is the count MOD 8 that particular option was incorrectly checked
   *    either separately or in conjunction with other incorrect options.
   *    option is counted from left to right, starting with 0. (reversed octal)
   * The number of X's is the number of multiple choice options.
   * This does not accumulate at non-leaf levels.
   */
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
        " TutorId: " + tutorId + 
        ", Time spent: " + stats[TIME_SPENT] + 
        ", NumAttempts: " + stats[NUM_ATTEMPTS] +
        ", numSuccesses: " + stats[NUM_SUCCESSES] +
        ", failureTracker: " + stats[FAILURE_TRACKER] + 
        ", percentProgress: " + stats[PERCENT_PROGRESS];
  }
}