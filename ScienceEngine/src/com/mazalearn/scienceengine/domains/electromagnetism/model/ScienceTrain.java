package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.mazalearn.scienceengine.core.model.Science2DBody;

/**
 * ScienceTrain represents the science train
 *
 * 
 * @author sridhar
 */
public class ScienceTrain extends Science2DBody {

  public ScienceTrain(float x, float y, float angle) {
    super(ComponentType.ScienceTrain, x, y, angle);
  }
  
  @Override
  public boolean allowsConfiguration() {
    return false;
  }
}
