package com.mazalearn.scienceengine.core.controller;

import com.mazalearn.scienceengine.core.model.IParameter;
import com.mazalearn.scienceengine.core.model.Science2DBody;

public interface IModelConfig<T> {
  public enum ConfigType { RANGE, LIST, COMMAND, TOGGLE, TEXT };
  
  public ConfigType getType();
  public String getName();
  // Get the parameter associated with this config.
  public IParameter getParameter();
  // Is this configuration hook available with current configs?
  public boolean isAvailable();
  // Is this a meter (can only be get) or a config (can be get/set through GUI)
  public boolean isMeter();

  // Only for COMMAND type
  public void doCommand();
  
  // get and set only for RANGE, LIST, TOGGLE types
  public T getValue();
  public void setValue(T value);
  
  // low, high only for RANGE type
  public float getLow();
  public float getHigh();
  
  // enums only for LIST type
  @SuppressWarnings("rawtypes")
  public Enum[] getList();
  public void setPermitted(boolean isPermitted);
  // Is manipulation of this config permitted in this context?
  public boolean isPermitted();
  // Is manipulation of this config possible consistent with this context?
  public boolean isPossible();
  // Does it have a probe mode?
  public boolean hasProbeMode();
  // Set probe mode - has to support if it has a probe mode
  public void setProbeMode();
  // Body for which this config is applicable
  public Science2DBody getBody();
}
