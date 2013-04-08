package com.mazalearn.scienceengine.domains.statesofmatter.model;

import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.IComponentType;

public enum ComponentType implements IComponentType {
  MoleculeBox, 
  ColorPanel;
  
  public String getLocalizedName() {
    return ScienceEngine.getMsg().getString("Name." + name());  
  }
  
  public static ComponentType valueOf(IComponentType cType) {
    for (ComponentType componentType: ComponentType.values()) {
      if (componentType.name().equals(cType.name())) {
        return componentType;
      }
    }
    return null;
  }
}
