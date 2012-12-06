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

  private final float low, high;    // Range
  @SuppressWarnings("rawtypes")
  private Enum[] values;            // List
  
  // Command type constructor
  public AbstractModelConfig(Science2DBody body, IParameter attribute) {
    this(ConfigType.COMMAND, body, attribute, false, 0, 0, null);
  }
  
  // Text type constructor
  public AbstractModelConfig(Science2DBody body, IParameter attribute, String text) {
    this(ConfigType.TEXT, body, attribute, false, 0, 0, null);
  }
  
  // Toggle type constructor
  public AbstractModelConfig(Science2DBody body, IParameter attribute, boolean on) {
    this(ConfigType.TOGGLE, body, attribute, on, 0, 0, null);
  }
  
  // Range type constructor
  public AbstractModelConfig(Science2DBody body, IParameter attribute, float low, float high) {
    this(ConfigType.RANGE, body, attribute, false, low, high, null);
  }
  
  // List type constructor
  @SuppressWarnings("rawtypes")
  public AbstractModelConfig(Science2DBody body, IParameter attribute, Enum[] values) {
    this(ConfigType.LIST, body, attribute, false, 0, 0, values);
  }
  
  // Canonical constructor - only used internally
  @SuppressWarnings("rawtypes")
  private AbstractModelConfig(ConfigType type, Science2DBody body, IParameter attribute, 
      boolean on, float low, float high, Enum[] values) {
    this.type = type;
    this.body = body;
    this.parameter = attribute;
    // Ignoring <code>on</code>;
    this.low = low;
    this.high = high;
    this.values = values;
    this.isPermitted = true;
  }
  
  @Override
  public void setPermitted(boolean isPermitted) {
    this.isPermitted = isPermitted;
  }
  
  public boolean isPermitted() { return isPermitted; }  
  public ConfigType getType() { return type; }
  public String getName() { 
    if (body != null) {
      return body.name() + "." + parameter.name();
    }
    return parameter.name();
  }
  public IParameter getParameter() { return parameter; }
  public T getValue() { return null; }
  public void setValue(T value) {}
  public abstract boolean isPossible();
  public boolean isAvailable() { 
    return isPermitted && isPossible() && body == ScienceEngine.getSelectedBody();
  }
  public boolean hasProbeMode() { return false; }
  public void setProbeMode() {}
  
  //  For Range type only
  public float getLow() { return low;}
  public float getHigh() { return high;}
  
  // For Command type only
  public void doCommand() {}
  
  // For List type only
  @SuppressWarnings("rawtypes")
  public Enum[] getList() { return values; }
}
