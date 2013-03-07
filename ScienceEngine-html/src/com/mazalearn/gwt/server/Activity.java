package com.mazalearn.gwt.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import com.google.gson.Gson;

public class Activity {
  public static class Tutor {
    public String type;
    public String id;
    public String goal;
    Tutor[] childtutors;
    public transient float timeSpent = 0;
    public transient float numAttempts = 0;
    public transient float numSuccesses = 0;
    public transient float failureTracker = 0;
    public transient float percentProgress = 0;
    private static final String NUM_ATTEMPTS = "numAttempts";
    private static final String NUM_SUCCESSES = "numSucceses";
    private static final String PERCENT_PROGRESS = "percentProgress";
    private static final String TIME_SPENT = "timeSpent";
    private static final String FAILURE_TRACKER = "failureTracker";
    
    private String makeTutorKey(int activity, String tutorId, String key) {
      return activity + "$" + tutorId + "$" + key;
    }

    private float getStat(Map<String, Float> stats, int activity, String key) {
      String statKey = makeTutorKey(activity, id, key);
      Float statValue = stats.get(statKey);
      statValue = statValue == null ? 0f : Math.round(statValue);
      return statValue;
      
    }
    public void loadStats(Map<String, Float> stats, Topic topic, int activityLevel) {
      timeSpent = getStat(stats, activityLevel, TIME_SPENT);
      numAttempts = getStat(stats, activityLevel, NUM_ATTEMPTS);
      numSuccesses = getStat(stats, activityLevel, NUM_SUCCESSES);
      failureTracker = getStat(stats, activityLevel, FAILURE_TRACKER);
      percentProgress = getStat(stats, activityLevel, PERCENT_PROGRESS);
      // printStats(topic, activityLevel);
    }
    
    private void printStats(Topic topic, int activityLevel) {
      String str = "Topic: " + topic.name() +
          " Level: " + activityLevel + 
          " Tutor: " + id + 
          ", Time spent: " + timeSpent + 
          ", NumAttempts: " + numAttempts +
          ", numSuccesses: " + numSuccesses +
          ", failureTracker: " + failureTracker + 
          ", percentProgress: " + percentProgress;
      System.out.println(str);
    }
  };
  String description;
  String name;
  int level;
  Tutor[] tutors;
  public transient List<Tutor> leafTutors;
  private transient Topic topic;
  private static final List<String> GROUP_TYPES = 
      Arrays.asList(new String[] {"Challenge", "RapidFire", "Guide"});
  
  public static Activity load(ServletContext servletContext, Topic topic, int activityLevel) {
    String json;
    String fileName = "/assets/data/" + topic.name() + "/" + activityLevel + ".json";
    InputStream inp = servletContext.getResourceAsStream(fileName);
    java.util.Scanner s = new java.util.Scanner(inp).useDelimiter("\\A");
    json = s.hasNext() ? s.next() : "";
    try {
      inp.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println(fileName + ": loading json");
    Activity activity = new Gson().fromJson(json, Activity.class);
    activity.topic = topic;
    activity.leafTutors = new ArrayList<Tutor>();
    collectLeafTutors(activity.tutors, activity.leafTutors);
    return activity;
  }
  
  public List<Tutor> getTutors() {
    return leafTutors;
  }
  
  public void populateStats(Map<String, Float> stats) {
    System.out.println("Populating stats");
    for (Tutor tutor: leafTutors) {
      tutor.loadStats(stats, topic, level);
    }  
  }

  private static void collectLeafTutors(Tutor[] tutors, List<Tutor> leafTutors) {
    if (tutors == null) return;
    
    for (Tutor child: tutors) {
      if (GROUP_TYPES.contains(child.type)) {
        collectLeafTutors(child.childtutors, leafTutors);
      } else {
        leafTutors.add(child);
      }
    }
  }
}
