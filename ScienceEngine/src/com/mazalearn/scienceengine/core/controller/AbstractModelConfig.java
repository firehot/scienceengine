package com.mazalearn.scienceengine.core.controller;

public abstract class AbstractModelConfig<T> implements IModelConfig<T> {
  
  private final ConfigType type;
  private final String name;
  private final String description;
  private boolean isPermitted;

  private final float low, high;    // Range
  private boolean on;         // OnOff
  @SuppressWarnings("rawtypes")
  private Enum[] values;            // List
  
  // Command type constructor
  public AbstractModelConfig(String name, String description) {
    this(ConfigType.COMMAND, name, description, false, 0, 0, null);
  }
  
  // OnOff constructor
  public AbstractModelConfig(String name, String description, boolean on) {
    this(ConfigType.ONOFF, name, description, on, 0, 0, null);
  }
  
  // Range type constructor
  public AbstractModelConfig(String name, String description, float low, float high) {
    this(ConfigType.RANGE, name, description, false, low, high, null);
  }
  
  // List type constructor
  @SuppressWarnings("rawtypes")
  public AbstractModelConfig(String name, String description, Enum[] values) {
    this(ConfigType.LIST, name, description, false, 0, 0, values);
  }
  
  // Canonical constructor - only used internally
  @SuppressWarnings("rawtypes")
  private AbstractModelConfig(ConfigType type, String name, String description, 
      boolean on, float low, float high, Enum[] values) {
    this.type = type;
    this.name = name;
    this.description = description;
    this.on = on;
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
  public abstract boolean isPossible();
  public boolean isAvailable() { return isPermitted && isPossible();}
  
  //  For Range type only
  public float getLow() { return low;}
  public float getHigh() { return high;}
  
  // For Command type only
  public void doCommand() {}
  
  // For List type only
  @SuppressWarnings("rawtypes")
  public Enum[] getList() { return values; }
}
