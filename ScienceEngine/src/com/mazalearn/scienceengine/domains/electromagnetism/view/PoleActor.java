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
import com.mazalearn.scienceengine.domains.electromagnetism.model.Pole;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Pole.PoleType;

public class PoleActor extends Science2DActor {
  private final Pole pole;
  private Image fieldArrow;
  Vector2 lastTouch = new Vector2(), currentTouch = new Vector2();
  private TextureRegion textureSouthPole;
  private TextureRegion textureNorthPole;
    
  public PoleActor(Science2DBody body, TextureRegion textureRegion) {
    super(body, textureRegion);
    this.pole = (Pole) body;
    this.removeListener(getListeners().get(0)); // help listener
    this.removeListener(getListeners().get(0)); // move, rotate listener
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
        fieldArrow.setSize(currentTouch.len(), currentTouch.len());
        fieldArrow.setOrigin(0,  fieldArrow.getHeight() / 2);
        fieldArrow.setPosition(getX() + getWidth() / 2, getY() + getHeight() / 2 - fieldArrow.getHeight() / 2);
        pole.setField(currentTouch);
      }
      
      @Override
      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        // No more field
        fieldArrow.setRotation(0);
        fieldArrow.setSize(0, 0);
        fieldArrow.setOrigin(0, 0);
        currentTouch.set(0, 0);
        pole.setField(currentTouch);
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
/*    List<FieldSample> fieldSamples = fieldMeter.getFieldSamples();
    // Reverse traversal - want to show the latest point first
    for (int i = fieldSamples.size() - 1; i >= 0; i--) {
      FieldSample fieldSample = fieldSamples.get(i);
      // Magnitude is scaled logarmthmically as width and height of arrow
      float magnitude = (float) Math.min(Math.log(1 + fieldSample.magnitude), 5);
      // location of field meter center is the sample point
      pos.set(fieldSample.x, fieldSample.y);
      pos.mul(ScreenComponent.PIXELS_PER_M);
      // find location of origin
      float originX = magnitude * getWidth() / 2;
      float originY = magnitude * getHeight() / 2;
      // Bottom of arrow position
      pos.sub(originX, originY);
      float rotation =  (fieldSample.angle * MathUtils.radiansToDegrees) % 360;
      batch.draw(getTextureRegion(), pos.x, pos.y, 
          originX, originY, getWidth() * magnitude, getHeight() * magnitude, 1, 1, rotation);
    }*/
    fieldArrow.layout();
    fieldArrow.draw(batch, parentAlpha);
    this.getTextureRegion().setRegion(
        pole.getPoleType() == PoleType.NorthPole ? textureNorthPole : textureSouthPole);
    super.draw(batch, parentAlpha);
  }
}