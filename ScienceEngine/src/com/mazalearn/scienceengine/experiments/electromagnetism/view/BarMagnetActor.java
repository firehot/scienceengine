package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.BarMagnet;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.BarMagnet.Mode;

public class BarMagnetActor extends Science2DActor {
  private final BarMagnet barMagnet;
  private BitmapFont font;
  private Vector2 newPos = new Vector2();
  
  public BarMagnetActor(Science2DBody body, TextureRegion textureRegion, 
      BitmapFont font) {
    super(body, textureRegion);
    this.barMagnet = (BarMagnet) body;
    this.font = font;
    this.setAllowMove(true);
    Array<EventListener> listeners = this.getListeners();
    if (listeners.size == 1) { // Remove the touch listener
      this.removeListener(listeners.first());
    }
    this.addListener(new ClickListener() {
      @Override
      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        if (Mode.valueOf(barMagnet.getMode()) != Mode.Rotate) return;
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
        getBox2DPositionFromViewPosition(newPos, newPos, getRotation());
        // Use center as origin - dont understand why this step
        newPos.sub(barMagnet.getWidth()/2, barMagnet.getHeight()/2);
        barMagnet.applyForce(lastTouch, newPos);
      }

      @Override
      public boolean touchDown(InputEvent event, float localX, float localY, int pointer, int button) {
        if (!isAllowMove()) return false;
        currentTouch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        getStage().getCamera().unproject(currentTouch);
        lastTouch.set(currentTouch.x, currentTouch.y);
        return true;
      }

      @Override
      public void touchDragged(InputEvent event, float localX, float localY, int pointer) {
        if (Mode.valueOf(barMagnet.getMode()) != Mode.Free) return;
        // Screen coords of current touch
        currentTouch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        // Screen coords of current touch
        getStage().getCamera().unproject(currentTouch);
        // Get negative of movement vector
        lastTouch.sub(currentTouch.x, currentTouch.y);
        setPosition(getX() - lastTouch.x, getY() - lastTouch.y);
        setPositionFromViewCoords(true);
        // Recalibrate lastTouch to new coordinates
        lastTouch.set(currentTouch.x, currentTouch.y);
      }
    });
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    if (Mode.valueOf(barMagnet.getMode()) == Mode.Rotate) { // Display RPM
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