package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.core.model.ScienceBody;
import com.mazalearn.scienceengine.core.view.ScienceActor;

public class PickupCoilActor extends ScienceActor {
  public PickupCoilActor(ScienceBody body, TextureRegion textureRegion) {
    super(body, textureRegion);
    this.setAllowDrag(true);
  }
}