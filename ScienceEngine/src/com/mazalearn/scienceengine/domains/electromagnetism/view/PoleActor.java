package com.mazalearn.scienceengine.domains.electromagnetism.view;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.FieldMeter;
import com.mazalearn.scienceengine.domains.electromagnetism.model.FieldMeter.FieldSample;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Pole;

public class PoleActor extends Science2DActor {
  private final Pole pole;
  private Vector2 pos = new Vector2();
  private Image fieldArrow;
    
  public PoleActor(Science2DBody body, TextureRegion textureRegion) {
    super(body, textureRegion);
    this.pole = (Pole) body;
    this.removeListener(getListeners().get(0)); // help listener
    this.removeListener(getListeners().get(0)); // move, rotate listener
    fieldArrow = new Image(ScienceEngine.getTextureRegion("fieldarrow-yellow"));
    this.addListener(new ClickListener() {
      Vector2 lastTouch = new Vector2(), currentTouch = new Vector2();
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
        pole.applyForce(currentTouch, pole.getWorldCenter());
        fieldArrow.setRotation(currentTouch.angle());
        fieldArrow.setSize(currentTouch.len(), currentTouch.len());
        fieldArrow.setOrigin(0,  fieldArrow.getHeight() / 2);
        fieldArrow.setPosition(getX() + getWidth() / 2, getY() + getHeight() / 2 - fieldArrow.getHeight() / 2);
      }
      
      @Override
      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
      }
    });
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
    super.draw(batch, parentAlpha);
    fieldArrow.layout();
    fieldArrow.draw(batch, parentAlpha);
  }
}