package com.mazalearn.scienceengine.experiments.controller;

public interface IConfig<T> {
  enum ConfigType { Float, String, Command };
  
  public ConfigType getType();
  public String getName();
  public String getDescription();
  // Is this configuration hook available with current configs?
  public boolean isAvailable();

  // Only for Command type
  public void doCommand();
  // get and set only for Float, String types
  public T getValue();
  public void setValue(T value);
  // low, high only for Float type
  public float getLow();
  public float getHigh();
  // enums only for String type
  @SuppressWarnings("rawtypes")
  public Enum[] getEnums();
}
