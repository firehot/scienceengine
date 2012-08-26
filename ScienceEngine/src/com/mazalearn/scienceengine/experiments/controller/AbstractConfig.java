package com.mazalearn.scienceengine.experiments.controller;

public abstract class AbstractConfig<T> implements IConfig<T> {
  
  private final ConfigType type;
  private final String name;
  private final String description;
  
  public AbstractConfig(ConfigType type, String name, String description) {
    this.type = type;
    this.name = name;
    this.description = description;
  }
  public ConfigType getType() { return type; }
  public String getName() { return name; }
  public String getDescription() { return description; }
  public T getValue() { return null; }
  public void setValue(T value) {}
  public void doCommand() {}
  public boolean isAvailable() { return true;}
}
