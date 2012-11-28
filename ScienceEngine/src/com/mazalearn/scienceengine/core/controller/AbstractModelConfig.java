package com.mazalearn.scienceengine.core.controller;

import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.IComponentType;
import com.mazalearn.scienceengine.core.model.Science2DBody;

public abstract class AbstractModelConfig<T> implements IModelConfig<T> {
  
  private final ConfigType type;
  private final Science2DBody body;
  // Not final, protected so that dummy probe will work
  protected IComponentType attribute;
  private boolean isPermitted;

  private final float low, high;    // Range
  @SuppressWarnings("rawtypes")
  private Enum[] values;            // List
  
  // Command type constructor
  public AbstractModelConfig(Science2DBody body, IComponentType attribute) {
    this(ConfigType.COMMAND, body, attribute, false, 0, 0, null);
  }
  
  // Text type constructor
  public AbstractModelConfig(Science2DBody body, IComponentType attribute, String text) {
    this(ConfigType.TEXT, body, attribute, false, 0, 0, null);
  }
  
  // Toggle type constructor
  public AbstractModelConfig(Science2DBody body, IComponentType attribute, boolean on) {
    this(ConfigType.TOGGLE, body, attribute, on, 0, 0, null);
  }
  
  // Range type constructor
  public AbstractModelConfig(Science2DBody body, IComponentType attribute, float low, float high) {
    this(ConfigType.RANGE, body, attribute, false, low, high, null);
  }
  
  // List type constructor
  @SuppressWarnings("rawtypes")
  public AbstractModelConfig(Science2DBody body, IComponentType attribute, Enum[] values) {
    this(ConfigType.LIST, body, attribute, false, 0, 0, values);
  }
  
  // Canonical constructor - only used internally
  @SuppressWarnings("rawtypes")
  private AbstractModelConfig(ConfigType type, Science2DBody body, IComponentType attribute, 
      boolean on, float low, float high, Enum[] values) {
    this.type = type;
    this.body = body;
    this.attribute = attribute;
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
  public Science2DBody getBody() { return body; }
  public String getName() { 
    if (body != null) {
      return body.name() + "." + attribute.name();
    }
    return attribute.name();
  }
  public IComponentType getAttribute() { return attribute; }
  public T getValue() { return null; }
  public void setValue(T value) {}
  public abstract boolean isPossible();
  public boolean isAvailable() { 
    return isPermitted && isPossible() && !ScienceEngine.isProbeMode() && body == ScienceEngine.getSelectedBody();
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
