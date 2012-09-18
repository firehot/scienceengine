package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.mazalearn.scienceengine.box2d.Box2DActor;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.FieldMeter;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.FieldMeter.FieldSample;

public class FieldSamplerView extends Box2DActor {
  private final FieldMeter fieldMeter;
    
  public FieldSamplerView(TextureRegion textureRegion, ScienceBody body) {
    super(body, textureRegion);
    this.fieldMeter = (FieldMeter) body;
    this.originX = width/2;
    this.originY = height/2;
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    Color c = batch.getColor();
    for (FieldSample fieldSample: fieldMeter.getFieldSamples()) {
      // Magnitude is scaled visually as color intensity
      batch.setColor(1, 1, 1, 0.75f  + fieldSample.magnitude * 100);
      float rotation =  (fieldSample.angle * MathUtils.radiansToDegrees) % 360;
      batch.draw(getTextureRegion(), fieldSample.x * PIXELS_PER_M, 
          fieldSample.y * PIXELS_PER_M, 
          0, 0, width, height, 1, 1, rotation);
    }
    batch.setColor(c);
  }
}