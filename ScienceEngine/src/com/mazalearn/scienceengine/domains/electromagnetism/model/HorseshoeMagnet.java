package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;

public class HorseshoeMagnet extends AbstractMagnet {

  private static final float B_FIELD_MAGNITUDE = 10.0f; // TODO: tune parameter
  private float strength = 5f;

  public HorseshoeMagnet(float x, float y, float angle) {
    super(ComponentType.HorseshoeMagnet, x, y, angle);
    this.setSize(32, 32);
    this.setStrength(B_FIELD_MAGNITUDE);
  }

  @Override
  protected Vector2 getBFieldRelative(Vector2 p, Vector2 outputVector) {
    outputVector.set(strength, 0);
    return outputVector;
  }

  @Override
  public void initializeConfigs() {
    configs.add(new AbstractModelConfig<Float>(this, 
        Attribute.MagnetStrength, 0f, 10f) {
      public Float getValue() { return getStrength(); }
      public void setValue(Float value) { setStrength(value); }
      public boolean isPossible() { return isActive(); }
    });

    configs.add(new AbstractModelConfig<Boolean>(this, 
        Attribute.Flip, false) {
      public Boolean getValue() { return getAngle() != 0f; }
      public void setValue(Boolean value) { setAngle(value ? MathUtils.PI : 0); }
      public boolean isPossible() { return isActive(); }
    });
  }
}
