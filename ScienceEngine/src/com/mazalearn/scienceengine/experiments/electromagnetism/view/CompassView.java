package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.box2d.Box2DActor;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Compass;

public class CompassView extends Box2DActor {
  private final Compass compass;
    
  public CompassView(TextureRegion textureRegion, ScienceBody body) {
    super(body, textureRegion);
    this.width /= 2;
    this.height /= 2;
    this.compass = (Compass) body;
    this.setOrigin(width/2, height/2);
    this.setAllowDrag(true);
  }
}