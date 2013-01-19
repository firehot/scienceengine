package com.mazalearn.scienceengine.core.controller;

import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.IParameter;
import com.mazalearn.scienceengine.core.model.Science2DBody;

public abstract class AbstractModelConfig<T> implements IModelConfig<T> {
  
  private final ConfigType type;
  private final Science2DBody body;
  // Not final, protected so that dummy probe will work
  protected IParameter parameter;
  private boolean isPermitted;

  private float low, high;    // Range
  @SuppressWarnings("rawtypes")
  private Enum[] values;            // List
  
  // Command type constructor
  public AbstractModelConfig(Science2DBody body, IParameter parameter) {
    this(ConfigType.COMMAND, body, parameter, false, 0, 0, null);
  }
  
  // Text type constructor
  public AbstractModelConfig(Science2DBody body, IParameter parameter, String text) {
    this(ConfigType.TEXT, body, parameter, false, 0, 0, null);
  }
  
  // Toggle type constructor
  public AbstractModelConfig(Science2DBody body, IParameter parameter, boolean on) {
    this(ConfigType.TOGGLE, body, parameter, on, 0, 0, null);
  }
  
  // Range type constructor
  public AbstractModelConfig(Science2DBody body, IParameter parameter, float low, float high) {
    this(ConfigType.RANGE, body, parameter, false, low, high, null);
  }
  
  // List type constructor
  @SuppressWarnings("rawtypes")
  public AbstractModelConfig(Science2DBody body, IParameter parameter, Enum[] values) {
    this(ConfigType.LIST, body, parameter, false, 0, 0, values);
  }
  
  // Canonical constructor - only used internally
  @SuppressWarnings("rawtypes")
  private AbstractModelConfig(ConfigType type, Science2DBody body, IParameter parameter, 
      boolean on, float low, float high, Enum[] values) {
    this.type = type;
    this.body = body;
    this.parameter = parameter;
    // Ignoring <code>on</code>;
    this.low = low;
    this.high = high;
    this.values = values;
    this.isPermitted = false;
  }
  
  @Override
  public void setPermitted(boolean isPermitted) {
    this.isPermitted = isPermitted;
  }
  
  @Override public boolean isPermitted() { return isPermitted; }  
  @Override public ConfigType getType() { return type; }
  @Override public String getName() { 
    if (body != null) {
      return body.name() + "." + parameter.name();
    }
    return parameter.name();
  }
  @Override public Science2DBody getBody() { return body; }
  @Override public IParameter getParameter() { return parameter; }
  @Override public T getValue() { return null; }
  @Override public void setValue(T value) {}
  @Override public abstract boolean isPossible();
  @Override public boolean isAvailable() { 
    return isPermitted && isPossible() && (body == ScienceEngine.getSelectedBody() || ScienceEngine.isPinned(body));
  }
  @Override public boolean hasProbeMode() { return false; }
  @Override public void setProbeMode() {}
  
  //  For Range type only
  @Override public float getLow() { return low;}
  @Override public float getHigh() { return high;}
  public void setRange(float low, float high) {
    this.low = low;
    this.high = high;
  }
  
  // For Command type only
  @Override public void doCommand() {}
  
  // For List type only
  @SuppressWarnings("rawtypes")
  @Override public Enum[] getList() { return values; }
}
