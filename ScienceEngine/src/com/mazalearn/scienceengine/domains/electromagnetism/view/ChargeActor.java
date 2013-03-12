package com.mazalearn.scienceengine.domains.electromagnetism.view;

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
import com.mazalearn.scienceengine.domains.electromagnetism.model.Monopole;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Monopole.MonopoleType;

public class ChargeActor extends Science2DActor {
  private final Monopole monopole;
  private Image fieldArrow;
  Vector2 lastTouch = new Vector2(), currentTouch = new Vector2();
  private TextureRegion textureSouthPole;
  private TextureRegion textureNorthPole;
    
  public ChargeActor(Science2DBody body, TextureRegion textureRegion) {
    super(body, textureRegion);
    this.monopole = (Monopole) body;
    this.removeListener(getListeners().get(0)); // help listener
    //this.removeListener(getListeners().get(0)); // move, rotate listener
    fieldArrow = new Image(ScienceEngine.getTextureRegion("arrow"));
    this.textureNorthPole = ScienceEngine.getTextureRegion("northpole");
    this.textureSouthPole = ScienceEngine.getTextureRegion("southpole");
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
        fieldArrow.setRotation(currentTouch.angle());
        // TODO: Put maxlimit on size.
        fieldArrow.setSize(currentTouch.len(), currentTouch.len());
        fieldArrow.setOrigin(0,  fieldArrow.getHeight() / 2);
        fieldArrow.setPosition(getX() + getWidth() / 2, getY() + getHeight() / 2 - fieldArrow.getHeight() / 2);
        monopole.setBField(currentTouch);
      }
      
      @Override
      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        // No more field
        fieldArrow.setRotation(0);
        fieldArrow.setSize(0, 0);
        fieldArrow.setOrigin(0, 0);
        currentTouch.set(0, 0);
        monopole.setBField(currentTouch);
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
    fieldArrow.layout();
    fieldArrow.draw(batch, parentAlpha);
    this.getTextureRegion().setRegion(
        monopole.getPoleType() == MonopoleType.NorthPole ? textureNorthPole : textureSouthPole);
    super.draw(batch, parentAlpha);
  }
}