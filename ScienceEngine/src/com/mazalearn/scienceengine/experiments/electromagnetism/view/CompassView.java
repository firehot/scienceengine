package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.box2d.Box2DActor;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Compass;
import com.mazalearn.scienceengine.view.AbstractExperimentView;

public class CompassView extends Box2DActor {
  private final Compass compass;
  private Vector2 lastTouch = new Vector2();
  private Vector2 newPos = new Vector2();
  
  public CompassView(TextureRegion textureRegion, ScienceBody body) {
    super(body, textureRegion);
    this.compass = (Compass) body;
    this.originX = width/2;
    this.originY = height/2;
  }

  public boolean touchDown(float x, float y, int pointer) {
    lastTouch.set(x, y);
    return true;
  }

  public void touchDragged(float x, float y, int pointer) {
    // New touch position
    newPos.set(x, y);
    // Subtract old touch position to get displacement vector
    newPos.sub(lastTouch);
    // Add displacement vector to the actor position to find new position
    newPos.add(this.x, this.y);
    // Scale down from actor coords to compass coords
    newPos.mul(1f/AbstractExperimentView.PIXELS_PER_M);
    // Move compass to new position
    compass.setPositionAndAngle(newPos, compass.getAngle());
    // Move towchDownPos into relative coordinates
    lastTouch.set(x, y);
  }
}