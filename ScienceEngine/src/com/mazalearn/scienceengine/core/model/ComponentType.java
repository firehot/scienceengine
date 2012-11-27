package com.mazalearn.scienceengine.core.model;

import com.mazalearn.scienceengine.ScienceEngine;

public enum ComponentType implements IComponentType {
  Dummy,
  Environment;
  
  public String toString() {
    return ScienceEngine.getMsg().getString("Name." + name());
  }
}