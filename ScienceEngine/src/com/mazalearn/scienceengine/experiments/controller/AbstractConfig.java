package com.mazalearn.scienceengine.experiments.controller;

public abstract class AbstractConfig<T> implements IConfig<T> {
  
  private final ConfigType type;
  private final String name;
  private final String description;
  private final float low, high;
  private Enum[] values;
  
  public AbstractConfig(String name, String description) {
    this(ConfigType.Command, name, description, 0, 0, null);
  }
  
  public AbstractConfig(String name, String description, float low, float high) {
    this(ConfigType.Float, name, description, low, high, null);
  }
  
  public AbstractConfig(String name, String description, Enum[] values) {
    this(ConfigType.String, name, description, 0, 0, values);
  }
    
  public AbstractConfig(ConfigType type, String name, String description, 
      float low, float high, Enum[] values) {
    this.type = type;
    this.name = name;
    this.description = description;
    this.low = low;
    this.high = high;
    this.values = values;
  }
  
  public ConfigType getType() { return type; }
  public String getName() { return name; }
  public String getDescription() { return description; }
  public T getValue() { return null; }
  public void setValue(T value) {}
  public float getLow() { return low;}
  public float getHigh() { return high;}
  public void doCommand() {}
  public boolean isAvailable() { return true;}
  @SuppressWarnings("rawtypes")
  public Enum[] getEnums() { return values; }
}
