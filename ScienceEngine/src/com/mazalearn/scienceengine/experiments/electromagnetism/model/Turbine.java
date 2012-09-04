// Copyright 2002-2011, University of Colorado

package com.mazalearn.scienceengine.experiments.electromagnetism.model;

/**
 * Turbine is the model of a simple turbine. It rotates at some speed, and its
 * rotation is measured in RPMs (rotations per minute).
 * 
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class Turbine extends BarMagnet {

  public static final int CLOCK_FRAME_RATE = 25; // frames per second

  private double speed; // -1...+1 (see setSpeed)
  private double maxRPM; // rotations per minute at full speed
  private double maxDelta; // change in angle at full speed, in radians

  private double direction;

  /**
   * Sole constructor.
   */
  public Turbine(EMField emField, float x, float y, float angle) {
    super(emField, x, y, angle);
    this.speed = 0.0;
    setMaxRPM(100.0);
  }

  @Override
  public String getName() {
    return "Turbine";
  }

  /**
   * Sets the speed. Speed is a value between -1.0 and +1.0 inclusive. The sign
   * of the value indicates angle. Zero is stopped, 1 is full speed.
   * 
   * @param speed
   *          the speed
   */
  public void setSpeed(double speed) {
    assert (speed >= -1 && speed <= 1);
    this.speed = speed;
  }

  /**
   * Gets the speed. See setSpeed.
   * 
   * @return the speed
   */
  public double getSpeed() {
    return this.speed;
  }

  /**
   * Sets the maximum rotations per minute.
   * 
   * @param maxRPM
   */
  public void setMaxRPM(double maxRPM) {
    this.maxRPM = maxRPM;

    // Pre-compute the maximum change in angle per clock tick.
    double framesPerSecond = CLOCK_FRAME_RATE;
    double framesPerMinute = 60 * framesPerSecond;
    this.maxDelta = (2 * Math.PI) * (maxRPM / framesPerMinute);
  }

  /**
   * Gets the maximum rotations per minute.
   * 
   * @return the maximum rotations per minute
   */
  public double getMaxRPM() {
    return this.maxRPM;
  }

  /**
   * Gets the number of rotations per minute at the current speed.
   * 
   * @return rotations per minute
   */
  public double getRPM() {
    return Math.abs(this.speed * this.maxRPM);
  }

  /*
   * Update the turbine's angle, based on its speed.
   */
  public void stepInTime(double dt) {

    if (this.speed != 0) {

      // Determine the new angle
      double delta = dt * this.speed * this.maxDelta;
      double newDirection = direction + delta;

      // Limit angle to -360...+360 degrees.
      int sign = (newDirection < 0) ? -1 : +1;
      newDirection = sign * (Math.abs(newDirection) % (2 * Math.PI));

      direction = newDirection;
    }
  }
}
