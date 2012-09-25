package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.ScienceBody;
import com.mazalearn.scienceengine.core.view.ScienceActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.FieldMeter;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.FieldMeter.FieldSample;

public class FieldMeterActor extends ScienceActor {
  private final FieldMeter fieldMeter;
  private Vector2 pos = new Vector2();
    
  public FieldMeterActor(TextureRegion textureRegion, ScienceBody body) {
    super(body, textureRegion);
    this.fieldMeter = (FieldMeter) body;
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