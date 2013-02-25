package com.mazalearn.gwt.server;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import com.google.gson.Gson;

public class Activity {
  public static class Tutor {
    public String type;
    public String id;
    public String goal;
    public String group;
    Tutor[] childTutors;
    public transient float successPercent;
    public transient float timeSpent;
  };
  String description;
  String name;
  int level;
  Tutor[] tutors;
  public transient List<Tutor> leafTutors;
  
  public static Activity load(ServletContext servletContext, String fileName) {
    String json;
    InputStream inp = servletContext.getResourceAsStream(fileName);
    java.util.Scanner s = new java.util.Scanner(inp).useDelimiter("\\A");
    json = s.hasNext() ? s.next() : "";
    Activity activity = new Gson().fromJson(json, Activity.class);
    activity.leafTutors = new ArrayList<Tutor>();
    collectLeafTutors(activity.tutors, activity.leafTutors);
    return activity;
  }
  
  public List<Tutor> getTutors() {
    return leafTutors;
  }
  
  public void populateStats(Map<String, Float> stats) {
    for (Tutor tutor: leafTutors) {
      String successPercentKey = "1$" + tutor.id + "$completionPercent";
      String timeSpentKey = "1$" + tutor.id + "$timeSpent";
      Float successPercent = stats.get(successPercentKey);
      tutor.successPercent = successPercent == null ? 0 : successPercent;
      Float timeSpent = stats.get(timeSpentKey);
      tutor.timeSpent = timeSpent == null ? 0 : Math.round(timeSpent);
    }  
  }

  private static void collectLeafTutors(Tutor[] tutors, List<Tutor> leafTutors) {
    if (tutors == null) return;
    
    for (Tutor child: tutors) {
      if (child.group == null || child.group.equals("None")) {
        leafTutors.add(child);
      } else {
        collectLeafTutors(child.childTutors, leafTutors);
      }
    }
  }
}
