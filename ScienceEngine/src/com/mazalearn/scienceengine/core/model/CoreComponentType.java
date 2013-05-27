package com.mazalearn.scienceengine.core.model;

import com.mazalearn.scienceengine.ScienceEngine;

public enum CoreComponentType implements IComponentType {
  Dummy,
  Drawing, 
  Environment,
  Global, 
  ScienceTrain, 
  Image;
  
  public String getLocalizedName() {
    return ScienceEngine.getMsg().getString("Name." + name());
  }
}