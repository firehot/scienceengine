package com.mazalearn.gwt.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import com.google.gson.Gson;
import com.mazalearn.scienceengine.Topic;

public class Activity {
  public static class Tutor {
    private static final int NUM_STATS = 5;
    public String type;
    public String id;
    public String goal;
    Tutor[] childtutors;
    public transient float[] stats = new float[NUM_STATS];
    public static final int NUM_ATTEMPTS = 0;
    public static final int NUM_SUCCESSES = 1;
    public static final int PERCENT_PROGRESS = 2;
    public static final int TIME_SPENT = 3;
    public static final int FAILURE_TRACKER = 4;
    
    public void loadStats(Map<String, float[]> stats, Topic topic, int activityLevel) {
      String statKey = activityLevel + "$" + id;
      float[] statValues = stats.get(statKey);
      if (statValues != null) this.stats = statValues;
    }    
  };
  
  String description;
  String name;
  int activityId;
  Tutor[] tutors;
  public transient List<Tutor> leafTutors;
  private transient Topic topic;
  private static final List<String> GROUP_TYPES = 
      Arrays.asList(new String[] {"Challenge", "RapidFire", "Guide"});
  
  public static Activity load(ServletContext servletContext, Topic topic, Topic activityLevel) {
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
    activity.activityId = activityLevel.getTopicId();
    activity.leafTutors = new ArrayList<Tutor>();
    collectLeafTutors(activity.tutors, activity.leafTutors);
    return activity;
  }
  
  public List<Tutor> getTutors() {
    return leafTutors;
  }
  
  public void populateStats(Map<String, float[]> stats) {
    System.out.println("Populating stats");
    for (Tutor tutor: leafTutors) {
      tutor.loadStats(stats, topic, activityId);
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
