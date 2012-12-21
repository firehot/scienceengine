package com.mazalearn.scienceengine.app.services;

import java.util.ArrayList;
import java.util.List;

import com.mazalearn.scienceengine.core.lang.Event;
import com.mazalearn.scienceengine.core.model.ComponentType;
import com.mazalearn.scienceengine.core.model.Parameter;

public class EventLog {
  List<Event> events = new ArrayList<Event>();
  private boolean suppressDuplicates = true;
  private Event lastEvent;
  
  public void logEvent(String object, String action) {
    logEvent(object, action, 0f);
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
    } else if ("Max".equals(function)) {
      // TODO: Use a visitor pattern.
      float max = -Float.MAX_VALUE;
      // Go in reverse order and stop on end of current subgoal
      for (int i = events.size() - 1; i >= 0; i--) {
        Event e = events.get(i);
        if (e.getObject().equals(ComponentType.Global.name()) &&
            e.getAction().equals(Parameter.Tutor.name())) {
          break;
        }
        if (e.getObject().equals(object) && e.getAction().equals(action)) {
          max = Math.max(max, e.fvalue());
        }
      }
      return max;
    } else if ("Min".equals(function)) {
      float min = Float.MAX_VALUE;
      // Go in reverse order and stop on end of current subgoal
      for (int i = events.size() - 1; i >= 0; i--) {
        Event e = events.get(i);
        if (e.getObject().equals(ComponentType.Global.name()) &&
            e.getAction().equals(Parameter.Tutor.name())) {
          break;
        }
        if (e.getObject().equals(object) && e.getAction().equals(action)) {
          min = Math.min(min, e.fvalue());
        }
      }
      return min;
    }
    return 0;
  }

  public void logEvent(String object, String action, float value) {
    if (suppressDuplicates && lastEvent != null && 
        lastEvent.getObject().equals(object) && 
        lastEvent.getAction().equals(action) &&
        (lastEvent.getType() == Event.Type.FLOAT && lastEvent.fvalue() == value)) {
      return;
    }
    Event event = new Event(object, action, value);
    events.add(event);
  }
}
