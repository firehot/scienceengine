package com.mazalearn.scienceengine.domains.mechanics.model;

import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.IComponentType;

public enum ComponentType implements IComponentType {
  SimpleBody("northpole");
  
  private String textureName;
  
  private ComponentType(String textureFilename) {
    this.textureName = textureFilename;
  }
  
  public String getTextureName() {
    return textureName;
  }
  
  public String toString() {
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
