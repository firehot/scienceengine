package com.mazalearn.scienceengine.app.services;

import java.util.ArrayList;
import java.util.List;

import com.mazalearn.scienceengine.core.model.IComponentType;
import com.mazalearn.scienceengine.core.model.Science2DBody;

public class EventLog {
  List<Event> events = new ArrayList<Event>();
  
  public enum EventType {
    SelectBody,
    SelectParameter;
  }
  private static class Event {
    private EventType eventType;
    private Science2DBody body;
    private IComponentType parameter;
    private long time;
    
    public Event(EventType eventType, Science2DBody body) {
      this(eventType, body, null);
    }
    
    public Event(EventType eventType, Science2DBody body,
        IComponentType parameter) {
      this.eventType = eventType;
      this.body = body;
      this.parameter = parameter;
      this.time = System.currentTimeMillis();
    }
    
  };
  
  public void logBodyEvent(Science2DBody body) {
    Event event = new Event(EventType.SelectBody, body);
    events.add(event);
  }

  public void logParameterEvent(Science2DBody body, IComponentType parameter) {
    Event event = new Event(EventType.SelectParameter, body, parameter);
    events.add(event);
  }
}
