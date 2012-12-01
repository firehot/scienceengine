package com.mazalearn.scienceengine.app.services;

import java.util.ArrayList;
import java.util.List;

import com.mazalearn.scienceengine.core.model.IParameter;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Parameter;

public class EventLog {
  List<Event> events = new ArrayList<Event>();
  
  private static class Event {
    private Science2DBody body;
    private IParameter parameter;
    private long time;
    
    public Event(Science2DBody body, IParameter parameter) {
      this.body = body;
      this.parameter = parameter;
      this.time = System.currentTimeMillis();
    }
    
  };
  
  public void logBodyEvent(Science2DBody body) {
    Event event = new Event(body, Parameter.Select);
    events.add(event);
  }

  public void logParameterEvent(Science2DBody body, IParameter parameter) {
    Event event = new Event(body, parameter);
    events.add(event);
  }
}
