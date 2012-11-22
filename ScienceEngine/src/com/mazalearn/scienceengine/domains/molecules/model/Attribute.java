package com.mazalearn.scienceengine.domains.molecules.model;

import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.IComponentType;

public enum Attribute implements IComponentType {
  StateOfMatter,
  HeatingLevel;
  
  private Attribute() {
  }
  
  public String toString() {
    return ScienceEngine.getPlatformAdapter().getMsg().getString("Name." + name());  
  }
  
  public static Attribute valueOf(IComponentType cType) {
    for (Attribute componentType: Attribute.values()) {
      if (componentType.name().equals(cType.name())) {
        return componentType;
      }
    }
    return null;
  }
}
