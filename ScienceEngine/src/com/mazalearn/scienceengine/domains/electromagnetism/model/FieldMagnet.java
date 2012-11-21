package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.badlogic.gdx.math.Vector2;

public class FieldMagnet extends AbstractMagnet {

  private static final float B_FIELD_MAGNITUDE = 10.0f; // TODO: tune parameter

  public FieldMagnet(float x, float y, float angle) {
    super(ComponentType.FieldMagnet, x, y, angle);
    this.setSize(8, 16);
    this.setStrength(B_FIELD_MAGNITUDE);
  }

  @Override
  protected Vector2 getBFieldRelative(Vector2 p, Vector2 outputVector) {
    if (p.y < getHeight() / 2 && p.y > -getHeight() / 2) {
      outputVector.set(10.0f, 0);
    }
    return outputVector;
  }

}
