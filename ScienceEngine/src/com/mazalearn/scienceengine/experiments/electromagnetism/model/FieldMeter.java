// Copyright 2002-2012, University of Colorado

package com.mazalearn.scienceengine.experiments.electromagnetism.model;

import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.box2d.ScienceBody;

/**
 * FieldMeter is the model of a meter to measure B-field.
 * 
 * @author sridhar
 */
public class FieldMeter extends ScienceBody implements EMField.IConsumer {

  // B-field vector at the field meter's position.
  private Vector2 fieldVector;

  public FieldMeter(EMField emField) {
    super("FieldMeter");

    this.fieldVector = new Vector2();
    emField.registerConsumer(this);
  }

  /**
   * Gets the strength at the field meter's position.
   * 
   * @param vector - strength value is copied here
   */
  public void getStrength(Vector2 vector /* output */) {
    vector.set(this.fieldVector);
  }

  /*
   * Updates the field meter's position and takes a B-field reading at that
   * position.
   */
  public void setBField(Vector2 bField) {
    this.fieldVector.set(bField);
  }
}
