package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.ScienceEngine;
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
  private Vector2 newPos = new Vector2();
  
  public BarMagnetView(TextureRegion textureRegion, ScienceBody body, 
      AbstractExperimentView experimentView, ElectroMagnetismModel emModel) {
    super(body, textureRegion);
    this.barMagnet = (BarMagnet) body;
    this.emView = experimentView;
    this.emModel = emModel;
    this.font = new BitmapFont();
    this.setOrigin(width/2, height/2);
    this.setAllowDrag(true);
  }

  public void touchDragged(float x, float y, int pointer) {
    if (Mode.valueOf(emModel.getMode()) != Mode.Free) return;
    super.touchDragged(x, y, pointer);
    emView.resume();
  }
  
  public void touchUp(float x, float y, int pointer) {
    if (Mode.valueOf(emModel.getMode()) != Mode.Rotate) return;
    // Screen coords of current touch
    currentTouch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
    // Goto view coords of current touch
    getStage().getCamera().unproject(currentTouch);
    // Get negative of movement vector
    lastTouch.sub(currentTouch.x, currentTouch.y);
    // Scale displacement vector suitably to get a proportional force
    lastTouch.mul(-10000);
    // view coords of current touch
    newPos.set(currentTouch.x, currentTouch.y);
    // box2d point of current touch
    getBox2DPositionFromViewPosition(newPos, newPos, rotation);
    // Use center as origin - dont understand why this step
    newPos.sub(barMagnet.getWidth()/2, barMagnet.getHeight()/2);
    barMagnet.applyForce(lastTouch, newPos);
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
    newPos.set(barMagnet.getWorldCenter()).mul(ScienceEngine.PIXELS_PER_M);
    // Create space for text - in screen coords and always left to right
    newPos.add(-10, 5);
    font.draw(batch, rpm, newPos.x, newPos.y);
  }
}