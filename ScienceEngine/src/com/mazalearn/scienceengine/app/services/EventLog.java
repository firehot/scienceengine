package com.mazalearn.scienceengine.app.services;

import java.util.ArrayList;
import java.util.List;

import com.mazalearn.scienceengine.core.model.IParameter;
import com.mazalearn.scienceengine.core.model.Science2DBody;

public class EventLog {
  List<Event> events = new ArrayList<Event>();
  private boolean suppressDuplicates = true;
  private Event lastEvent;
  
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
  
  public void logEvent(Science2DBody body, IParameter parameter) {
    if (suppressDuplicates && lastEvent != null && 
        lastEvent.body == body && lastEvent.parameter == parameter) {
      return;
    }
    Event event = new Event(body, parameter);
    events.add(event);
  }
}
