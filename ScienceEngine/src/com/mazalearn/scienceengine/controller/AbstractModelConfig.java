package com.mazalearn.scienceengine.controller;

public abstract class AbstractModelConfig<T> implements IModelConfig<T> {
  
  private final ConfigType type;
  private final String name;
  private final String description;
  private final float low, high;
  private boolean isPermitted;
  @SuppressWarnings("rawtypes")
  private Enum[] values;
  
  public AbstractModelConfig(String name, String description) {
    this(ConfigType.COMMAND, name, description, 0, 0, null);
  }
  
  public AbstractModelConfig(String name, String description, float low, float high) {
    this(ConfigType.RANGE, name, description, low, high, null);
  }
  
  @SuppressWarnings("rawtypes")
  public AbstractModelConfig(String name, String description, Enum[] values) {
    this(ConfigType.LIST, name, description, 0, 0, values);
  }
    
  @SuppressWarnings("rawtypes")
  public AbstractModelConfig(ConfigType type, String name, String description, 
      float low, float high, Enum[] values) {
    this.type = type;
    this.name = name;
    this.description = description;
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
  public String getName() { return name; }
  public String getDescription() { return description; }
  public T getValue() { return null; }
  public void setValue(T value) {}
  public float getLow() { return low;}
  public float getHigh() { return high;}
  public void doCommand() {}
  public boolean isAvailable() { return isPermitted && isPossible();}
  public abstract boolean isPossible();
  @SuppressWarnings("rawtypes")
  public Enum[] getList() { return values; }
}
