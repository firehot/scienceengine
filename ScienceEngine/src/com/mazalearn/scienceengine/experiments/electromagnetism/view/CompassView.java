package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.box2d.ScienceActor;
import com.mazalearn.scienceengine.box2d.ScienceBody;

public class CompassView extends ScienceActor {
  public CompassView(TextureRegion textureRegion, ScienceBody body) {
    super(body, textureRegion);
    this.setAllowDrag(true);
  }
}