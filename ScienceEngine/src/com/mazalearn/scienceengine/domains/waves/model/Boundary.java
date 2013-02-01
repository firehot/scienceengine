package com.mazalearn.scienceengine.domains.waves.model;

import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.model.IComponentType;
import com.mazalearn.scienceengine.core.model.Science2DBody;

public class Boundary extends Science2DBody {
  // Enum used for different BoundaryActor Conditions on end of string
  public enum BoundaryType { FixedEnd, LooseEnd, NoEnd };
  // BoundaryType Condition of other end.
  private BoundaryType boundaryType = BoundaryType.FixedEnd;

  public Boundary(IComponentType componentType, float x, float y, float angle) {
    super(componentType, x, y, angle);
  }

  public String getBoundaryType() {
    return boundaryType.name();
  }

  public void setBoundaryType(String endType) {
    this.boundaryType = BoundaryType.valueOf(endType);
    reset();
  }

  @Override
  public void initializeConfigs() {
    configs.add(new AbstractModelConfig<String>(this, Parameter.BoundaryType, BoundaryType.values()) {
      public String getValue() { return getBoundaryType(); }
      public void setValue(String value) { setBoundaryType(value); }
      public boolean isPossible() { return true; }
    });
  }

  public BoundaryType boundaryType() {
    return boundaryType;
  }
}
