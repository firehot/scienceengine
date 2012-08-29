// Copyright 2002-2011, University of Colorado

package com.mazalearn.scienceengine.experiments.electromagnetism.model;

import com.mazalearn.scienceengine.box2d.ScienceBody;

/**
 * AbstractCurrentSource is the abstract base class for all things that are
 * capable of acting as a current source.
 * 
 * @author sridhar
 */
public abstract class AbstractCurrentSource extends ScienceBody {

  private static final float DEFAULT_MAX_VOLTAGE = Float.POSITIVE_INFINITY;

  private float maxVoltage;
  private float amplitude;

  /**
   * Sole constructor.
   */
  public AbstractCurrentSource(String name) {
    super(name);
    this.maxVoltage = DEFAULT_MAX_VOLTAGE;
    this.amplitude = 1f; // full strength
  }

  /**
   * Gets the voltage.
   * 
   * @return the voltage, in volts
   */
  public float getVoltage() {
    return this.amplitude * this.maxVoltage;
  }

  /*
   * NOTE! There is intentionally no setVoltage method; do NOT add one. Voltage
   * must be controlled via setAmplitude.
   */

  /**
   * Sets the maximum voltage that this voltage source will produce.
   * 
   * @param maxVoltage - the maximum voltage, in volts
   */
  public void setMaxVoltage(float maxVoltage) {
    this.maxVoltage = maxVoltage;
  }

  /**
   * Gets the maximum voltage that this voltage source will produce.
   * 
   * @return the maximum voltage, in volts
   */
  public float getMaxVoltage() {
    return this.maxVoltage;
  }

  /**
   * Sets the voltage amplitude. This indicates how the voltage relates to the
   * maximum voltage.
   * 
   * @param amplitude
   *          -1...+1
   */
  public void setAmplitude(float amplitude) {
    assert (amplitude >= -1 && amplitude <= 1);
    this.amplitude = amplitude;
  }

  /**
   * Gets the voltage amplitude. This indicates how the voltage relates to the
   * maximum voltage.
   * 
   * @return the amplitude, -1...+1
   */
  public float getAmplitude() {
    return this.amplitude;
  }
}
