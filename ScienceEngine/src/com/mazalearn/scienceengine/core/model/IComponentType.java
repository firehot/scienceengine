package com.mazalearn.scienceengine.core.model;

public interface IComponentType {
  // Canonical name: uses == 
  public String name();
  // localized version
  public String getLocalizedName();
}
