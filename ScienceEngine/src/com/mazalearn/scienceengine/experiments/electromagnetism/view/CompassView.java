package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.box2d.Box2DActor;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Compass;
import com.mazalearn.scienceengine.view.AbstractExperimentView;

public class CompassView extends Box2DActor {
  private final Compass compass;
  private Vector2 touchDownPos = new Vector2();
  private Vector2 newPos = new Vector2();
  private TextureRegion textureRegion;
  
  public CompassView(TextureRegion textureRegion, ScienceBody body) {
    super(body, textureRegion);
    this.compass = (Compass) body;
    this.textureRegion = textureRegion;
  }

  public boolean touchDown(float x, float y, int pointer) {
    touchDownPos.set(x, y);
    return true;
  }

  public void touchDragged(float x, float y, int pointer) {
    // New touch position
    newPos.set(x, y);
    // Subtract old touch position to get displacement vector
    newPos.sub(touchDownPos);
    // Add displacement vector to the actor position to find new position
    newPos.add(this.x, this.y);
    // Find center of compass in new position
    newPos.add(width/2, height/2);
    // Scale down from actor coords to compass coords
    newPos.mul(1f/AbstractExperimentView.PIXELS_PER_M);
    // Move compass to new position
    compass.setPositionAndAngle(newPos, compass.getAngle());
    // Move towchDownPos into relative coordinates
    touchDownPos.set(x, y);
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    // Find view position of left bottom corner of bar Magnet
    newPos.set(-compass.width/2, -compass.height/2);
    newPos.set(compass.getWorldPoint(newPos));
    newPos.mul(AbstractExperimentView.PIXELS_PER_M);
    this.x = newPos.x;
    this.y = newPos.y;
    this.rotation = (compass.getAngle() * MathUtils.radiansToDegrees) % 360;
    batch.draw(textureRegion, x, y, 0, 0, width, height, 0.25f, 0.25f, rotation);
  }   
}