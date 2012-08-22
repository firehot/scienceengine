// Copyright 2002-2011, University of Colorado

package com.mazalearn.scienceengine.experiments.model.electromagnetism;

import com.mazalearn.scienceengine.box2d.ScienceBody;

/**
 * Lightbulb is the model of a lightbulb. Its intensity is a function of the
 * current in the pickup coil.
 * 
 * @author sridhar
 */
public class Lightbulb extends ScienceBody {

  /* Absolute current amplitude below this value is treated as zero. */
  public static final double CURRENT_AMPLITUDE_THRESHOLD = 0.001;
  // ----------------------------------------------------------------------------
  // Instance data
  // ----------------------------------------------------------------------------

  private PickupCoil pickupCoilModel;
  private double previousCurrentAmplitude;

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
    this.previousCurrentAmplitude = 0.0;
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

    double intensity = 0.0;

    final double currentAmplitude = pickupCoilModel.getCurrentAmplitude();

    if ((currentAmplitude > 0 && previousCurrentAmplitude <= 0) || 
        (currentAmplitude <= 0 && previousCurrentAmplitude > 0)) {
      // Current changed angle, so turn the light off.
      intensity = 0.0;
    } else {
      // Light intensity is proportional to amplitude of current in the coil.
      intensity = Math.abs(currentAmplitude);

      // Intensity below the threshold is effectively zero.
      if (intensity < CURRENT_AMPLITUDE_THRESHOLD) {
        intensity = 0;
      }
    }

    previousCurrentAmplitude = currentAmplitude;

    assert (intensity >= 0 && intensity <= 1);
    return (float) intensity;
  }
}