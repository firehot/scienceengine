package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.box2d.ScienceActor;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.FieldMeter;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.FieldMeter.FieldSample;

public class FieldMeterView extends ScienceActor {
  private final FieldMeter fieldMeter;
  private Vector2 pos = new Vector2();
    
  public FieldMeterView(TextureRegion textureRegion, ScienceBody body) {
    super(body, textureRegion);
    this.fieldMeter = (FieldMeter) body;
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    List<FieldSample> fieldSamples = fieldMeter.getFieldSamples();
    // Reverse traversal - want to show the latest point first
    for (int i = fieldSamples.size() - 1; i >= 0; i--) {
      FieldSample fieldSample = fieldSamples.get(i);
      // Magnitude is scaled visually as width and height of arrow
      float magnitude = 2 * (fieldSample.magnitude > 2f ? 1f : fieldSample.magnitude);
      // location of field meter center is the sample point
      pos.set(fieldSample.x, fieldSample.y);
      pos.mul(ScienceEngine.PIXELS_PER_M);
      // find location of origin
      float originX = magnitude * width / 2;
      float originY = magnitude * height / 2;
      // Bottom of arrow position
      pos.sub(originX, originY);
      float rotation =  (fieldSample.angle * MathUtils.radiansToDegrees) % 360;
      batch.draw(getTextureRegion(), pos.x, pos.y, 
          originX, originY, width * magnitude, height * magnitude, 1, 1, rotation);
    }
  }
}