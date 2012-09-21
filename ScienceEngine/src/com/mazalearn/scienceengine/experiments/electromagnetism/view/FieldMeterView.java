package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.box2d.Box2DActor;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.FieldMeter;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.FieldMeter.FieldSample;

public class FieldMeterView extends Box2DActor {
  private final FieldMeter fieldMeter;
  private Vector2 pos = new Vector2();
    
  public FieldMeterView(TextureRegion textureRegion, ScienceBody body) {
    super(body, textureRegion);
    this.fieldMeter = (FieldMeter) body;
    this.setOrigin(width/2, height/2);
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    List<FieldSample> fieldSamples = fieldMeter.getFieldSamples();
    // Reverse traversal - want to show the latest point first
    for (int i = fieldSamples.size() - 1; i >= 0; i--) {
      FieldSample fieldSample = fieldSamples.get(i);
      // Magnitude is scaled visually as color intensity
      float magnitude = 2 * (fieldSample.magnitude > 2f ? 1f : fieldSample.magnitude);
      // location of field meter center is the sample point
      pos.set(fieldSample.x, fieldSample.y);
      pos.mul(ScienceEngine.PIXELS_PER_M);
      // find location of left bottom of arrow
      pos.sub(magnitude * MathUtils.cos(fieldSample.angle), 
          magnitude * MathUtils.sin(fieldSample.angle));
      float rotation =  (fieldSample.angle * MathUtils.radiansToDegrees) % 360;
      batch.draw(getTextureRegion(), pos.x, pos.y, 
          0, 0, width * magnitude, height * magnitude, 1, 1, rotation);
    }
  }
}