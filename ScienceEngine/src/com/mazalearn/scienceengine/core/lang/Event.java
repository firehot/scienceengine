package com.mazalearn.scienceengine.core.lang;

import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.IParameter;
import com.mazalearn.scienceengine.core.model.Science2DBody;

public class Event {
  private Science2DBody body;
  IParameter parameter;
  // Time of execution of science engine
  private float time;
  
  public Event(Science2DBody body, IParameter parameter) {
    this.body = body;
    this.parameter = parameter;
    this.time = ScienceEngine.getTime();
  }

  public Science2DBody getBody() {
    return body;
  }

  public IParameter getParameter() {
    return parameter;
  }

  public float getTime() {
    return time;
  }
}