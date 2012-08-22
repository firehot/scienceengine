// Copyright 2002-2011, University of Colorado

package com.mazalearn.scienceengine.experiments.model.electromagnetism;

import com.mazalearn.scienceengine.box2d.ScienceBody;

/**
 * Voltmeter is the model of an analog voltmeter. It's needle deflection is a
 * function of the current in the pickup coil. It uses an ad hoc algorithm that
 * makes the needle wobble around the zero point.
 * 
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class Voltmeter extends ScienceBody {

  /* Absolute current amplitude below this value is treated as zero. */
  public static final double CURRENT_AMPLITUDE_THRESHOLD = 0.001;

  // Define the zero point of the needle.
  private static final double ZERO_NEEDLE_ANGLE = Math.toRadians(0.0);

  // The needle deflection range is this much on either side of the zero point.
  private static final double MAX_NEEDLE_ANGLE = Math.toRadians(90.0);

  // If rotational kinematics is enabled, the needle will jiggle this much
  // around the zero reading.
  private static final double NEEDLE_JIGGLE_ANGLE = Math.toRadians(3.0);

  // When the angle is this close to zero, the needle stops jiggling.
  private static final double NEEDLE_JIGGLE_THRESHOLD = Math.toRadians(0.5);

  /*
   * Determines how much the needle jiggles around the zero point. The value L
   * should be such that 0 < L < 1. If set to 0, the needle will not jiggle at
   * all. If set to 1, the needle will ocsillate forever.
   */
  private static final double NEEDLE_LIVELINESS = 0.6;

  // Pickup coil that the voltmeter is connected to.
  private PickupCoil pickupCoilModel;

  // Whether the needle jiggles around its zero point.
  private boolean jiggleEnabled;

  // Needle deflection angle
  private double needleAngle;

  /**
   * Sole constructor.
   * 
   * @param pickupCoilModel
   *          voltmeter is connected to this pickup coil
   */
  public Voltmeter(PickupCoil pickupCoilModel) {
    super();
    jiggleEnabled = false; // expensive, so disabled by default
    needleAngle = ZERO_NEEDLE_ANGLE;
  }

  @Override
  public String getName() {
    return "VoltMeter";
  }

  /**
   * Enables/disabled jiggle behavior. This turns on an ad hoc algorithm that
   * causes the needle to jiggle at its zero point.
   * 
   * @param enabled
   *          true to enable, false to disable
   */
  public void setJiggleEnabled(boolean jiggleEnabled) {
    this.jiggleEnabled = jiggleEnabled;
  }

  /**
   * Determines whether jiggle behavior is enabled.
   * 
   * @return true if enabled, false if disabled
   */
  public boolean isJiggleEnabled() {
    return jiggleEnabled;
  }

  /**
   * Sets the needle's deflection angle.
   * 
   * @param needleAngle
   *          the angle, in radians
   */
  protected void setNeedleAngle(double needleAngle) {
    needleAngle = Clamp
        .clamp(-MAX_NEEDLE_ANGLE, needleAngle, +MAX_NEEDLE_ANGLE);
    this.needleAngle = needleAngle;
  }

  /**
   * Gets the needle's deflectin angle.
   * 
   * @return the angle, in radians
   */
  public double getNeedleAngle() {
    return needleAngle;
  }

  /**
   * Gets the desired needle deflection angle. This is the angle that
   * corresponds exactly to the voltage read by the meter.
   * 
   * @return the angle, in radians
   */
  private double getDesiredNeedleAngle() {

    // Use amplitude of the voltage source as our signal.
    double amplitude = pickupCoilModel.getCurrentAmplitude();

    // Absolute amplitude below the threshold is effectively zero.
    if (Math.abs(amplitude) < CURRENT_AMPLITUDE_THRESHOLD) {
      amplitude = 0;
    }

    // Determine the needle deflection angle.
    return amplitude * MAX_NEEDLE_ANGLE;
  }

  // ----------------------------------------------------------------------------
  // ModelElement implementation
  // ----------------------------------------------------------------------------

  /*
   * Updates the needle deflection angle. If rotational kinematics are enabled,
   * jiggle the needle around the zero point.
   * 
   * @see edu.colorado.phet.common.model.ModelElement#stepInTime(double)
   */
  public void singleStep(double dt) {
    // Determine the desired needle deflection angle.
    double needleAngle = getDesiredNeedleAngle();

    if (!jiggleEnabled) {
      // If jiggle is disabled, simply set the needle angle.
      setNeedleAngle(needleAngle);
    } else {
      // If jiggle is enabled, make the needle jiggle around the zero point.
      if (needleAngle != ZERO_NEEDLE_ANGLE) {
        setNeedleAngle(needleAngle);
      } else {
        double delta = getNeedleAngle();
        if (delta == 0) {
          // Do nothing, the needle is "at rest".
        } else if (Math.abs(delta) < NEEDLE_JIGGLE_THRESHOLD) {
          // The needle is close enought to "at rest".
          setNeedleAngle(ZERO_NEEDLE_ANGLE);
        } else {
          // Jiggle the needle around the zero point.
          double jiggleAngle = -delta * NEEDLE_LIVELINESS;
          jiggleAngle = Clamp.clamp(-NEEDLE_JIGGLE_ANGLE, jiggleAngle,
              +NEEDLE_JIGGLE_ANGLE);
          setNeedleAngle(jiggleAngle);
        }
      }
    }
  }
}
