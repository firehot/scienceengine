package com.mazalearn.scienceengine.experiments.controller;

public interface IConfig<T> {
  enum ConfigType { Float, String, Command };
  
  public ConfigType getType();
  public String getName();
  public String getDescription();
  public T getValue();
  public void setValue(T value);
  public void doCommand(); // Only for Command type
}
