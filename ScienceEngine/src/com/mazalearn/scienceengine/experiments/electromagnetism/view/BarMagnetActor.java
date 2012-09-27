package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.AbstractExperimentView;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.ElectroMagnetismModel;
import com.mazalearn.scienceengine.experiments.electromagnetism.ElectroMagnetismModel.Mode;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.BarMagnet;

public class BarMagnetActor extends Science2DActor {
  private final BarMagnet barMagnet;
  private final AbstractExperimentView emView;
  private final ElectroMagnetismModel emModel;
  private BitmapFont font;
  private Vector2 newPos = new Vector2();
  
  public BarMagnetActor(TextureRegion textureRegion, Science2DBody body, 
      AbstractExperimentView experimentView, ElectroMagnetismModel emModel) {
    super(body, textureRegion);
    this.barMagnet = (BarMagnet) body;
    this.emView = experimentView;
    this.emModel = emModel;
    this.font = new BitmapFont();
    this.setAllowDrag(true);
  }

  public void touchDragged(float x, float y, int pointer) {
    if (Mode.valueOf(emModel.getMode()) != Mode.Free) return;
    super.touchDragged(x, y, pointer);
  }
  
  public void touchUp(float x, float y, int pointer) {
    if (Mode.valueOf(emModel.getMode()) != Mode.Rotate) return;
    // Screen coords of currentProber touch
    currentTouch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
    // Goto view coords of currentProber touch
    getStage().getCamera().unproject(currentTouch);
    // Get negative of movement vector
    lastTouch.sub(currentTouch.x, currentTouch.y);
    // Scale displacement vector suitably to get a proportional force
    lastTouch.mul(-10000);
    // view coords of currentProber touch
    newPos.set(currentTouch.x, currentTouch.y);
    // box2d point of currentProber touch
    getBox2DPositionFromViewPosition(newPos, newPos, rotation);
    // Use center as origin - dont understand why this step
    newPos.sub(barMagnet.getWidth()/2, barMagnet.getHeight()/2);
    barMagnet.applyForce(lastTouch, newPos);
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
    newPos.set(barMagnet.getWorldCenter()).mul(ScienceEngine.PIXELS_PER_M);
    // Create space for text - in screen coords and always left to right
    newPos.add(-10, 5);
    font.draw(batch, rpm, newPos.x, newPos.y);
  }
}