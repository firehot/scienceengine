package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.box2d.Box2DActor;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.experiments.electromagnetism.ElectroMagnetismModel;
import com.mazalearn.scienceengine.experiments.electromagnetism.ElectroMagnetismModel.Mode;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.BarMagnet;
import com.mazalearn.scienceengine.view.AbstractExperimentView;

public class BarMagnetView extends Box2DActor {
  private final BarMagnet barMagnet;
  private final AbstractExperimentView emView;
  private final ElectroMagnetismModel emModel;
  private BitmapFont font;
  private Vector2 lastTouch = new Vector2();
  private Vector2 newPos = new Vector2();
  
  public BarMagnetView(TextureRegion textureRegion, ScienceBody body, 
      AbstractExperimentView experimentView, ElectroMagnetismModel emModel) {
    super(body, textureRegion);
    this.barMagnet = (BarMagnet) body;
    this.emView = experimentView;
    this.emModel = emModel;
    this.font = new BitmapFont();
    this.originX = width/2;
    this.originY = height/2;
  }

  public boolean touchDown(float x, float y, int pointer) {
    lastTouch.set(x, y);
    return true;
  }

  public void touchDragged(float x, float y, int pointer) {
    if (Mode.valueOf(emModel.getMode()) != Mode.Free) return;
    // New touch position
    newPos.set(x, y);
    // Subtract last touch position to get displacement vector
    newPos.sub(lastTouch);
    // Add displacement vector to the actor position to find new position
    this.x += newPos.x;
    this.y += newPos.y;
    setPositionFromScreen();
    // Recalibrate lastTouch to new coordinates
    lastTouch.set(x, y);
    emView.resume();
  }
  
  public void touchUp(float x, float y, int pointer) {
    if (Mode.valueOf(emModel.getMode()) != Mode.Rotate) return;
    // new touch position
    newPos.set(x, y);
    // Subtract old touch position to get displacement vector
    newPos.sub(lastTouch);
    // Scale displacement vector suitably to get a proportional force
    newPos.mul(20000);
    // Apply the force at point touched in world coords
    lastTouch.sub(width/2, height/2);
    lastTouch.mul(1f/AbstractExperimentView.PIXELS_PER_M);
    barMagnet.applyForce(newPos, barMagnet.getWorldPoint(lastTouch));
    emView.resume();
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    if (Mode.valueOf(emModel.getMode()) == Mode.Rotate) { // Display RPM
      drawRpm(batch, parentAlpha);
    }
  }

  private void drawRpm(SpriteBatch batch, float parentAlpha) {
    font.setColor(1f, 1f, 1f, parentAlpha);
    int angularVelocity = Math.round(barMagnet.getAngularVelocity());
    String rpm = String.valueOf(angularVelocity);
    newPos.set(barMagnet.getWorldCenter());
    newPos.mul(AbstractExperimentView.PIXELS_PER_M);
    newPos.add(-10, 5);
    font.draw(batch, rpm, newPos.x + originX, newPos.y + originY);
  }
}