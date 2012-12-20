package com.mazalearn.scienceengine.core.lang;

import com.mazalearn.scienceengine.ScienceEngine;

public class Event {
  public enum Type {FLOAT, STRING, BOOLEAN};
  private Type type;
  private String object;
  private String action;
  private float fvalue;
  private boolean bvalue;
  private String svalue;
  // Time of execution of science engine
  private float time;
  
  public Event(String object, String action, float value) {
    this(object, action, value, "", false);
    this.type = Type.FLOAT;
  }
  
  public Event(String object, String action, String value) {
    this(object, action, 0f, value, false);
    this.type = Type.STRING;
  }
  
  public Event(String object, String action, boolean value) {
    this(object, action, 0f, "", value);
    this.type = Type.BOOLEAN;
  }
  
  private Event(String object, String action, float fvalue, String svalue, boolean bvalue) {
    this.object = object;
    this.action = action;
    this.fvalue = fvalue;
    this.svalue = svalue;
    this.bvalue = bvalue;
    this.time = ScienceEngine.getTime();
  }

  public String getObject() {
    return object;
  }

  public String getAction() {
    return action;
  }

  public float fvalue() {
    switch(type) {
    case STRING: return 0f;
    case FLOAT: return fvalue;
    case BOOLEAN: return 0f;
    }
    return 0f;
  }

  public String svalue() {
    switch(type) {
    case STRING: return svalue;
    case FLOAT: return "";
    case BOOLEAN: return "";
    }
    return null;
  }

  public boolean bvalue() {
    switch(type) {
    case STRING: return false;
    case FLOAT: return false;
    case BOOLEAN: return bvalue;
    }
    return false;
  }

  public float getTime() {
    return time;
  }

  public Type getType() {
    return type;
  }
}