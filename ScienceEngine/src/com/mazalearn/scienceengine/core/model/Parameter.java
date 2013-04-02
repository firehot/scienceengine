package com.mazalearn.scienceengine.core.model;

import com.mazalearn.scienceengine.ScienceEngine;

public enum Parameter implements IParameter {
  Self,
  Reset,
  Tutoring,
  PauseResume,
  Select,
  Move,
  NameOfSelectedBody, 
  Rotate, 
  Tutor, 
  RotationAngle;
  
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
