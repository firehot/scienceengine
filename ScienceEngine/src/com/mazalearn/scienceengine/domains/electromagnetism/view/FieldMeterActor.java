package com.mazalearn.scienceengine.domains.electromagnetism.view;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.FieldMeter;
import com.mazalearn.scienceengine.domains.electromagnetism.model.FieldMeter.FieldSample;

public class FieldMeterActor extends Science2DActor {
  private final FieldMeter fieldMeter;
  private Vector2 pos = new Vector2();
    
  public FieldMeterActor(Science2DBody body, TextureRegion textureRegion) {
    super(body, textureRegion);
    this.fieldMeter = (FieldMeter) body;
    this.removeListener(getListeners().get(0)); // help listener
    this.removeListener(getListeners().get(0)); // move, rotate listener
    this.addListener(new ClickListener() {   
      @Override
      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        super.touchUp(event, x, y, pointer, button);
        ScienceEngine.selectBody(fieldMeter, (IScience2DView) getStage());
        // Move field sampler here and convert to model coords
        pos.set(event.getStageX(), event.getStageY()).mul(1f / ScienceEngine.PIXELS_PER_M);
        fieldMeter.setPositionAndAngle(pos, 0);
      }
    });
  }
  
  @Override
  public Actor hit (float x, float y, boolean touchable) {
    if (touchable && this.getTouchable() != Touchable.enabled) return null;
    // If nothing else hits, and fieldmeter is present, it shows a hit.
    // We exclude the top title and bottom status bars
    // Operate directly on input coords since x,y received here are wrt FieldMeter
    // and hence irrelevant
    getStage().screenToStageCoordinates(pos.set(Gdx.input.getX(), Gdx.input.getY()));
    return pos.y >= 20 && pos.y < AbstractScreen.VIEWPORT_HEIGHT - 30 ? this : null;
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    List<FieldSample> fieldSamples = fieldMeter.getFieldSamples();
    // Reverse traversal - want to show the latest point first
    for (int i = fieldSamples.size() - 1; i >= 0; i--) {
      FieldSample fieldSample = fieldSamples.get(i);
      // Magnitude is scaled logarmthmically as width and height of arrow
      float magnitude = (float) Math.min(Math.log(1 + fieldSample.magnitude), 5);
      // location of field meter center is the sample point
      pos.set(fieldSample.x, fieldSample.y);
      pos.mul(ScienceEngine.PIXELS_PER_M);
      // find location of origin
      float originX = magnitude * getWidth() / 2;
      float originY = magnitude * getHeight() / 2;
      // Bottom of arrow position
      pos.sub(originX, originY);
      float rotation =  (fieldSample.angle * MathUtils.radiansToDegrees) % 360;
      batch.draw(getTextureRegion(), pos.x, pos.y, 
          originX, originY, getWidth() * magnitude, getHeight() * magnitude, 1, 1, rotation);
    }
  }
}