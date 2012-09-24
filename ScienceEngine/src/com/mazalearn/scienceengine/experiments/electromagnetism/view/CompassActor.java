package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.core.model.ScienceBody;
import com.mazalearn.scienceengine.core.view.ScienceActor;

public class CompassActor extends ScienceActor {
  public CompassActor(TextureRegion textureRegion, ScienceBody body) {
    super(body, textureRegion);
    this.setAllowDrag(true);
  }
}