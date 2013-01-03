package com.mazalearn.scienceengine.app.services;

import java.util.ArrayList;
import java.util.List;

import com.mazalearn.scienceengine.app.services.Function.Aggregator;
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

  public float eval(Aggregator aggregator, String name) {
    int pos = name.lastIndexOf(".");
    String object = name.substring(0, pos);
    String action = name.substring(pos + 1);
    aggregator.init();
    // Go in reverse order and stop on end of current subgoal
    for (int i = events.size() - 1; i >= 0; i--) {
      Event e = events.get(i);
      if (e.getObject().equals(ComponentType.Global.name()) &&
          e.getAction().equals(Parameter.Tutor.name())) {
        break;
      }
      if (e.getObject().equals(object) && e.getAction().equals(action)) {
        aggregator.visit(e.fvalue());
      }
    }
    return aggregator.getValue();
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

  public void clear() {
    events.clear();
  }
}
