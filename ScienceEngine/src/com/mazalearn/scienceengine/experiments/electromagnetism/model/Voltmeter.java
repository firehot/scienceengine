// Copyright 2002-2011, University of Colorado

package com.mazalearn.scienceengine.experiments.electromagnetism.model;

import com.badlogic.gdx.math.MathUtils;
import com.mazalearn.scienceengine.box2d.ScienceBody;

/**
 * Voltmeter is the model of an analog voltmeter. It's needle deflection is a
 * function of the current in the pickup coil. It uses an ad hoc algorithm that
 * makes the needle wobble around the zero point.
 * 
 * @author sridhar
 */
public class Voltmeter extends ScienceBody {

  /* Absolute current amplitude below this value is treated as zero. */
  public static final float CURRENT_AMPLITUDE_THRESHOLD = 0.001f;

  // Define the zero point of the needle.
  private static final float ZERO_NEEDLE_ANGLE = 0;

  // The needle deflection range is this much on either side of the zero point.
  private static final float MAX_NEEDLE_ANGLE = MathUtils.degreesToRadians * 90;

  // If rotational kinematics is enabled, the needle will jiggle this much
  // around the zero reading.
  private static final float NEEDLE_JIGGLE_ANGLE = MathUtils.degreesToRadians * 3;

  // When the angle is this close to zero, the needle stops jiggling.
  private static final float NEEDLE_JIGGLE_THRESHOLD = MathUtils.degreesToRadians * 0.5f;

  /*
   * Determines how much the needle jiggles around the zero point. The value L
   * should be such that 0 < L < 1. If set to 0, the needle will not jiggle at
   * all. If set to 1, the needle will ocsillate forever.
   */
  private static final float NEEDLE_LIVELINESS = 0.6f;

  // Pickup coil that the voltmeter is connected to.
  private PickupCoil pickupCoilModel;

  // Whether the needle jiggles around its zero point.
  private boolean jiggleEnabled;

  // Needle deflection angle
  private float needleAngle;

  /**
   * Sole constructor.
   * 
   * @param pickupCoilModel
   *          voltmeter is connected to this pickup coil
   */
  public Voltmeter(PickupCoil pickupCoilModel, float x, float y, float angle) {
    super("Voltmeter", x, y, angle);
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
   * @param needleAngle - the angle, in radians
   */
  protected void setNeedleAngle(float needleAngle) {
    needleAngle = Clamp
        .clamp(-MAX_NEEDLE_ANGLE, needleAngle, +MAX_NEEDLE_ANGLE);
    this.needleAngle = needleAngle;
  }

  /**
   * Gets the needle's deflectin angle.
   * 
   * @return the angle, in radians
   */
  public float getNeedleAngle() {
    return needleAngle;
  }

  /**
   * Gets the desired needle deflection angle. This is the angle that
   * corresponds exactly to the voltage read by the meter.
   * 
   * @return the angle, in radians
   */
  private float getDesiredNeedleAngle() {

    // Use amplitude of the voltage source as our signal.
    float amplitude = pickupCoilModel.getCurrentAmplitude();

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
  public void singleStep(float dt) {
    // Determine the desired needle deflection angle.
    float needleAngle = getDesiredNeedleAngle();

    if (!jiggleEnabled) {
      // If jiggle is disabled, simply set the needle angle.
      setNeedleAngle(needleAngle);
    } else {
      // If jiggle is enabled, make the needle jiggle around the zero point.
      if (needleAngle != ZERO_NEEDLE_ANGLE) {
        setNeedleAngle(needleAngle);
      } else {
        float delta = getNeedleAngle();
        if (delta == 0) {
          // Do nothing, the needle is "at rest".
        } else if (Math.abs(delta) < NEEDLE_JIGGLE_THRESHOLD) {
          // The needle is close enought to "at rest".
          setNeedleAngle(ZERO_NEEDLE_ANGLE);
        } else {
          // Jiggle the needle around the zero point.
          float jiggleAngle = -delta * NEEDLE_LIVELINESS;
          jiggleAngle = Clamp.clamp(-NEEDLE_JIGGLE_ANGLE, jiggleAngle,
              +NEEDLE_JIGGLE_ANGLE);
          setNeedleAngle(jiggleAngle);
        }
      }
    }
  }
}
