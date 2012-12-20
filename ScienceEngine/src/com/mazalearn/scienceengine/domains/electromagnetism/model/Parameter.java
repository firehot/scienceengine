package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.IComponentType;
import com.mazalearn.scienceengine.core.model.IParameter;

public enum Parameter implements IParameter {
  CurrentType,
  CurrentFrequency,
  CurrentMax,
  CurrentDirection,
  MagnetStrength,
  MagnetRotation,
  MagnetMode,
  CommutatorType,
  CoilLoops,
  Flip, 
  RotationDataType, 
  RotationData, 
  Count, 
  Current;
  
  private Parameter() {
  }
  
  public String toString() {
    return ScienceEngine.getMsg().getString("Name." + name());  
  }
  
  public static Parameter valueOf(IComponentType cType) {
    for (Parameter componentType: Parameter.values()) {
      if (componentType.name().equals(cType.name())) {
        return componentType;
      }
    }
    return null;
  }
}
