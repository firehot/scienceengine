// Copyright 2002-2011, University of Colorado

package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.badlogic.gdx.math.MathUtils;
import com.mazalearn.scienceengine.core.model.Science2DBody;

/**
 * Voltmeter is the model of an analog voltmeter. It's needle deflection is a
 * function of the current in the pickup coil.
 * 
 * @author sridhar
 */
public class Voltmeter extends Science2DBody {

  /* Absolute current amplitude below this value is treated as zero. */
  public static final float CURRENT_AMPLITUDE_THRESHOLD = 0.001f;

  // Define the zero point of the needle.
  private static final float ZERO_NEEDLE_ANGLE = 0;

  // The needle deflection range is this much on either side of the zero point.
  private static final float MAX_NEEDLE_ANGLE = MathUtils.degreesToRadians * 90;

  // Pickup coil that the voltmeter is connected to.
  private PickupCoil pickupCoilModel;

  // Needle deflection angle
  private float needleAngle;

  /**
   * 
   * @param pickupCoilModel - voltmeter is connected to this pickup coil
   */
  public Voltmeter(PickupCoil pickupCoilModel, float x, float y, float angle) {
    super(ComponentType.Voltmeter, x, y, angle);
    needleAngle = ZERO_NEEDLE_ANGLE;
  }

  /**
   * Sets the needle's deflection angle.
   * 
   * @param needleAngle - the angle, in radians
   */
  protected void setNeedleAngle(float needleAngle) {
    this.needleAngle = 
        Clamp.clamp(-MAX_NEEDLE_ANGLE, needleAngle, +MAX_NEEDLE_ANGLE);
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
  private float computeNeedleAngle() {

    // Use amplitude of the voltage source as our signal.
    float amplitude = pickupCoilModel.getCurrent();

    // Absolute amplitude below the threshold is effectively zero.
    if (Math.abs(amplitude) < CURRENT_AMPLITUDE_THRESHOLD) {
      amplitude = 0;
    }

    // Determine the needle deflection angle.
    return amplitude * MAX_NEEDLE_ANGLE;
  }

  /*
   * Updates the needle deflection angle.
   */
  public void singleStep(float dt) {
    // Determine the desired needle deflection angle.
    setNeedleAngle(computeNeedleAngle());
  }
}
