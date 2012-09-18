package com.mazalearn.scienceengine.controller;

public interface IModelConfig<T> {
  enum ConfigType { RANGE, LIST, COMMAND, ONOFF };
  
  public ConfigType getType();
  public String getName();
  public String getDescription();
  // Is this configuration hook available with current configs?
  public boolean isAvailable();

  // Only for COMMAND type
  public void doCommand();
  
  // get and set only for RANGE, LIST, ONOFF types
  public T getValue();
  public void setValue(T value);
  
  // low, high only for RANGE type
  public float getLow();
  public float getHigh();
  
  // enums only for LIST type
  @SuppressWarnings("rawtypes")
  public Enum[] getList();
  public void setPermitted(boolean isPermitted);
  public boolean isPermitted();
  public boolean isPossible();
}
