package com.mazalearn.scienceengine.app.services;

import java.util.ArrayList;
import java.util.List;

import com.mazalearn.scienceengine.core.lang.Event;
import com.mazalearn.scienceengine.core.model.ComponentType;
import com.mazalearn.scienceengine.core.view.Parameter;

public class EventLog {
  List<Event> events = new ArrayList<Event>();
  private boolean suppressDuplicates = true;
  private Event lastEvent;
  
  public void logEvent(String object, String action) {
    if (suppressDuplicates && lastEvent != null && 
        lastEvent.getObject().equals(object) && lastEvent.getAction().equals(action)) {
      return;
    }
    Event event = new Event(object, action);
    events.add(event);
  }

  public float eval(String function, String name) {
    int pos = name.lastIndexOf(".");
    String object = name.substring(0, pos);
    String action = name.substring(pos + 1);
    if ("Count".equals(function)) {
      int count = 0;
      // Go in reverse order and stop on end of current subgoal
      for (int i = events.size() - 1; i >= 0; i--) {
        Event e = events.get(i);
        if (e.getObject().equals(ComponentType.Environment.name()) &&
            e.getAction().equals(Parameter.Challenge.name())) {
          break;
        }
        if (e.getObject().equals(object) && e.getAction().equals(action)) {
          count++;
        }
      }
      return count;
    }
    return 0;
  }
}
