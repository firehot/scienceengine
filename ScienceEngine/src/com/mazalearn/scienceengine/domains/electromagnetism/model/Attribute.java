package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.IComponentType;

public enum Attribute implements IComponentType {
  CurrentType,
  CurrentFrequency,
  CurrentMax,
  CurrentDirection,
  MagnetStrength,
  MagnetRotation,
  MagnetMode,
  CommutatorType,
  CoilLoops,
  Flip;
  
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