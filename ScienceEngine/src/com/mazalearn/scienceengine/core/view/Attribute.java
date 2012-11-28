package com.mazalearn.scienceengine.core.view;

import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.IComponentType;

public enum Attribute implements IComponentType {
  Reset,
  Challenge,
  PauseResume,
  NameOfSelectedBody;
  
  private Attribute() {
  }
  
  public String toString() {
    return ScienceEngine.getMsg().getString("Name." + name());  
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