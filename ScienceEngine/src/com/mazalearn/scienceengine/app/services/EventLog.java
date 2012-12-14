package com.mazalearn.scienceengine.app.services;

import java.util.ArrayList;
import java.util.List;

import com.mazalearn.scienceengine.core.lang.Event;
import com.mazalearn.scienceengine.core.model.IParameter;
import com.mazalearn.scienceengine.core.model.Science2DBody;

public class EventLog {
  List<Event> events = new ArrayList<Event>();
  private boolean suppressDuplicates = true;
  private Event lastEvent;
  
  public void logEvent(Science2DBody body, IParameter parameter) {
    if (suppressDuplicates && lastEvent != null && 
        lastEvent.getBody() == body && lastEvent.getParameter() == parameter) {
      return;
    }
    Event event = new Event(body, parameter);
    events.add(event);
  }
}
