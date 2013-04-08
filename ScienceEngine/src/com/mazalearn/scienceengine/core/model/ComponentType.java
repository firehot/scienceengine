package com.mazalearn.scienceengine.core.model;

import com.mazalearn.scienceengine.ScienceEngine;

public enum ComponentType implements IComponentType {
  Dummy,
  Environment,
  Global, 
  Image;
  
  public String getLocalizedName() {
    return ScienceEngine.getMsg().getString("Name." + name());
  }
}