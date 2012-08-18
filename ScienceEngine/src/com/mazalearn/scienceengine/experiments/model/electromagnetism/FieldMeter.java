// Copyright 2002-2012, University of Colorado

package com.mazalearn.scienceengine.experiments.model.electromagnetism;

import com.badlogic.gdx.math.Vector2;

/**
 * FieldMeter is the model of a meter to measure B-field.
 * 
 * @author sridhar
 */
public class FieldMeter extends FieldElement implements EMField.IConsumer {

  // ----------------------------------------------------------------------------
  // Instance data
  // ----------------------------------------------------------------------------

  // B-field vector at the field meter's location.
  private Vector2 fieldVector;

  // ----------------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------------

  public FieldMeter(EMField emField) {
    super();

    this.fieldVector = new Vector2();
    emField.registerConsumer(this);
  }

  // ----------------------------------------------------------------------------
  // Accessors
  // ----------------------------------------------------------------------------

  /**
   * Gets the strength at the field meter's location.
   * 
   * @param vector
   *          strength value is copied here
   */
  public void getStrength(Vector2 vector /* output */) {
    assert (vector != null);
    vector.set(this.fieldVector);
  }

  /*
   * Updates the field meter's location and takes a B-field reading at that
   * location.
   */
  public void setBField(Vector2 bField) {
    this.fieldVector.set(bField);
  }
}
