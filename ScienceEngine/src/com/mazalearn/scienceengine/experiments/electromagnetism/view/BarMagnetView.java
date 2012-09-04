package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
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
  private TextureRegion textureRegion;
  
  public BarMagnetView(TextureRegion textureRegion, ScienceBody body, 
      AbstractExperimentView experimentView, ElectroMagnetismModel emModel) {
    super(body, textureRegion);
    this.barMagnet = (BarMagnet) body;
    this.textureRegion = textureRegion;
    this.emView = experimentView;
    this.emModel = emModel;
    this.font = new BitmapFont();
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
    newPos.add(this.x, this.y);
    // Find center of bar Magnet in new position
    newPos.add(width/2, height/2);
    // Scale down from actor coords to barMagnet coords
    newPos.mul(1f/AbstractExperimentView.PIXELS_PER_M);
    // Move barMagnet to new position
    barMagnet.setPositionAndAngle(newPos, barMagnet.getAngle());
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
    newPos.mul(2000);
    // Apply the force at point touched in world coords
    lastTouch.sub(width/2, height/2);
    lastTouch.mul(1f/AbstractExperimentView.PIXELS_PER_M);
    barMagnet.applyForce(newPos, barMagnet.getWorldPoint(lastTouch));
    emView.resume();
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    // Find view position of left bottom corner of bar Magnet
    newPos.set(-barMagnet.getWidth()/2, -barMagnet.getHeight()/2);
    newPos.set(barMagnet.getWorldPoint(newPos));
    newPos.mul(AbstractExperimentView.PIXELS_PER_M);
    this.x = newPos.x;
    this.y = newPos.y;
    this.rotation = (barMagnet.getAngle() * MathUtils.radiansToDegrees) % 360;
    batch.draw(textureRegion, x, y, 0, 0, width, height, 1, 1, rotation);

    if (Mode.valueOf(emModel.getMode()) == Mode.Rotate) { // Display RPM
      font.setColor(1f, 1f, 1f, parentAlpha);
      String rpm = String.valueOf(Math.floor(barMagnet.getAngularVelocity()));
      newPos.set(barMagnet.getWorldCenter());
      newPos.mul(AbstractExperimentView.PIXELS_PER_M);
      newPos.add(-10, 5);
      font.draw(batch, rpm, newPos.x, newPos.y);
    }
  }   
}