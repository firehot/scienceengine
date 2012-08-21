// Copyright 2002-2011, University of Colorado

package com.mazalearn.scienceengine.experiments.model.electromagnetism;

import com.mazalearn.scienceengine.box2d.ScienceBody;

/**
 * Lightbulb is the model of a lightbulb. Its intensity is a function of the
 * current in the pickup coil.
 * 
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class Lightbulb extends ScienceBody {

  /* Absolute current amplitude below this value is treated as zero. */
  public static final double CURRENT_AMPLITUDE_THRESHOLD = 0.001;
  // ----------------------------------------------------------------------------
  // Instance data
  // ----------------------------------------------------------------------------

  private PickupCoil _pickupCoilModel;
  private double _previousCurrentAmplitude;
  private boolean _offWhenCurrentChangesDirection;

  // ----------------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------------

  /**
   * Sole constructor.
   * 
   * @param pickupCoilModel
   *          the pickup coil that the lightbulb is across
   */
  public Lightbulb(PickupCoil pickupCoilModel) {
    super();

    _pickupCoilModel = pickupCoilModel;

    _previousCurrentAmplitude = 0.0;
    _offWhenCurrentChangesDirection = false;
  }

  // ----------------------------------------------------------------------------
  // Accessors
  // ----------------------------------------------------------------------------

  /**
   * Gets the intensity of the light. Fully off is 0.0, fully on is 1.0.
   * 
   * @return the intensity (0.0 - 1.0)
   */
  public double getIntensity() {

    double intensity = 0.0;

    final double currentAmplitude = _pickupCoilModel.getCurrentAmplitude();

    if (_offWhenCurrentChangesDirection
        && ((currentAmplitude > 0 && _previousCurrentAmplitude <= 0) || (currentAmplitude <= 0 && _previousCurrentAmplitude > 0))) {
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

    _previousCurrentAmplitude = currentAmplitude;

    assert (intensity >= 0 && intensity <= 1);
    return intensity;
  }

  /**
   * Determines whether the lightbulb turns off when the current in the coil
   * changes angle. In some cases (eg, the Generator or AC Electromagnet)
   * this is the desired behavoir. In other cases (eg, polarity file of the Bar
   * Magnet) this is not the desired behavior.
   * 
   * @param offWhenCurrentChangesDirection
   *          true or false
   */
  public void setOffWhenCurrentChangesDirection(
      boolean offWhenCurrentChangesDirection) {
    _offWhenCurrentChangesDirection = offWhenCurrentChangesDirection;
  }

  /**
   * Determines whether the lightbulb turns off when the current in the coil
   * changes angle.
   * 
   * @return true or false
   */
  public boolean isOffWhenCurrentChangesDirection() {
    return _offWhenCurrentChangesDirection;
  }
}