package com.mazalearn.scienceengine.domains.mechanics.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.mechanics.model.SimpleBody;

public class SimpleBodyActor extends Science2DActor {
  private final SimpleBody simpleBody;
  private Image forceArrow;
  Vector2 lastTouch = new Vector2(), currentTouch = new Vector2();
    
  public SimpleBodyActor(Science2DBody body, TextureRegion textureRegion) {
    super(body, textureRegion);
    this.simpleBody = (SimpleBody) body;
    this.removeListener(getListeners().get(0)); // help listener
    this.removeListener(getListeners().get(0)); // move, rotate listener
    forceArrow = new Image(ScienceEngine.getTextureRegion("arrow"));
    this.addListener(new ClickListener() {
      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        lastTouch.set(x, y);
        return true;
      }
      
      @Override
      public void touchDragged(InputEvent event, float x, float y, int pointer) {
        currentTouch.set(x, y);
        currentTouch.sub(lastTouch);
        // Set Magnetic field based on drag position relative to touchdown point
        forceArrow.setRotation(currentTouch.angle());
        // TODO: Put maxlimit on size.
        forceArrow.setSize(currentTouch.len(), currentTouch.len());
        forceArrow.setOrigin(0,  forceArrow.getHeight() / 2);
        forceArrow.setPosition(getX() + getWidth() / 2, getY() + getHeight() / 2 - forceArrow.getHeight() / 2);
        simpleBody.setForce(currentTouch);
      }
      
      @Override
      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        // No more field
        forceArrow.setRotation(0);
        forceArrow.setSize(0, 0);
        forceArrow.setOrigin(0, 0);
        currentTouch.set(0, 0);
        simpleBody.setForce(currentTouch);
      }
    });
  }
  
  @Override
  public void act(float delta) {
    super.act(delta);
    // Wrap around
    if (getX() < 0 || getX() > ScreenComponent.VIEWPORT_WIDTH || 
        getY() < 0 || getY() > ScreenComponent.VIEWPORT_HEIGHT) {
      setX((getX() + ScreenComponent.VIEWPORT_WIDTH) % ScreenComponent.VIEWPORT_WIDTH);
      setY((getY() + ScreenComponent.VIEWPORT_HEIGHT) % ScreenComponent.VIEWPORT_HEIGHT);
      this.setPositionFromViewCoords(false);
    }
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    forceArrow.layout();
    forceArrow.draw(batch, parentAlpha);
    super.draw(batch, parentAlpha);
  }
}