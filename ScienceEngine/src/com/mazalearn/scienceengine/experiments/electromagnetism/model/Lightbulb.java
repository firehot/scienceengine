// Copyright 2002-2011, University of Colorado

package com.mazalearn.scienceengine.experiments.electromagnetism.model;

import com.mazalearn.scienceengine.box2d.ScienceBody;

/**
 * Lightbulb is the model of a lightbulb. Its intensity is a function of the
 * current in the pickup coil.
 * 
 * @author sridhar
 */
public class Lightbulb extends ScienceBody {

  /* Absolute current amplitude below this value is treated as zero. */
  public static final double CURRENT_AMPLITUDE_THRESHOLD = 0.01;
  // ----------------------------------------------------------------------------
  // Instance data
  // ----------------------------------------------------------------------------

  private PickupCoil pickupCoilModel;
  private float previousCurrentAmplitude;

  // ----------------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------------

  /**
   * Sole constructor.
   * 
   * @param pickupCoilModel - the pickup coil that the lightbulb is across
   */
  public Lightbulb(PickupCoil pickupCoilModel) {
    super();

    this.pickupCoilModel = pickupCoilModel;
    this.previousCurrentAmplitude = 0f;
  }

  public String getName() {
    return "Lightbulb";
  }

  /**
   * Gets the intensity of the light. Fully off is 0.0, fully on is 1.0.
   * 
   * @return the intensity (0.0 - 1.0)
   */
  public float getIntensity() {

    float intensity = 0f;

    final float currentAmplitude = pickupCoilModel.getCurrentAmplitude();

    // If current changed angle, turn the light off.
    if (Math.signum(currentAmplitude) != Math.signum(previousCurrentAmplitude)) {
      intensity = 0f;
    } else if (Math.abs(currentAmplitude)  < CURRENT_AMPLITUDE_THRESHOLD){
      // Intensity below the threshold is effectively zero.
      intensity = 0f;
    } else {
      // Light intensity is proportional to amplitude of current in the coil.
      intensity = currentAmplitude;
    }

    previousCurrentAmplitude = currentAmplitude;

    assert (intensity >= 0 && intensity <= 1);
    return intensity;
  }
}