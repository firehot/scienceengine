package com.mazalearn.scienceengine.core.lang;

import com.mazalearn.scienceengine.ScienceEngine;

public class Event {
  private String object;
  private String action;
  // Time of execution of science engine
  private float time;
  
  public Event(String object, String action) {
    this.object = object;
    this.action = action;
    this.time = ScienceEngine.getTime();
  }

  public String getObject() {
    return object;
  }

  public String getAction() {
    return action;
  }

  public float getTime() {
    return time;
  }
}