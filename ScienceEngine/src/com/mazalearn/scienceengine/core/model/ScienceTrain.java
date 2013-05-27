package com.mazalearn.scienceengine.core.model;

/**
 * ScienceTrain represents the science train
 *
 * 
 * @author sridhar
 */
public class ScienceTrain extends Science2DBody {

  public ScienceTrain(float x, float y, float angle) {
    super(CoreComponentType.ScienceTrain, x, y, angle);
  }
  
  @Override
  public boolean allowsConfiguration() {
    return false;
  }
}
