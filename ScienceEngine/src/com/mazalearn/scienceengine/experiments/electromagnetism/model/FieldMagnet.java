package com.mazalearn.scienceengine.experiments.electromagnetism.model;

import com.badlogic.gdx.math.Vector2;

public class FieldMagnet extends AbstractMagnet {

  private static final float B_FIELD_MAGNITUDE = 0.10f; // TODO: tune parameter

  public FieldMagnet(String name, float x, float y, float angle) {
    super(ComponentType.FieldMagnet, name, x, y, angle);
    this.setSize(16, 16);
  }

  @Override
  protected Vector2 getBFieldRelative(Vector2 p, Vector2 outputVector) {
    if (p.y < getHeight()) {
      outputVector.set(B_FIELD_MAGNITUDE, 0);
    }
    return outputVector;
  }

}
