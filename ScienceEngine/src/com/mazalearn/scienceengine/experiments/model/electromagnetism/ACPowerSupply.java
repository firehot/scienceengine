// Copyright 2002-2011, University of Colorado

package com.mazalearn.scienceengine.experiments.model.electromagnetism;

/**
 * ACPowerSupply is the model of an AC Power Supply.
 * <p>
 * The AC Power Supply has a configurable maximum voltage. A client varies the
 * maximum voltage amplitude and frequency. The voltage amplitude varies over
 * time.
 * 
 * @author sridhar
 */
public class ACPowerSupply extends AbstractCurrentSource {

  // ----------------------------------------------------------------------------
  // Class data
  // ----------------------------------------------------------------------------

  // The minimum number of steps used to approximate one sine wave cycle.
  private static final double MIN_STEPS_PER_CYCLE = 10;

  // ----------------------------------------------------------------------------
  // Instance data
  // ----------------------------------------------------------------------------

  // Determines how high the amplitude can go. (0...1 inclusive)
  private double maxAmplitude;
  // Determines how fast the amplitude will vary. (0...1 inclusive)
  private double frequency;
  // The current angle of the sine wave that describes the AC. (radians)
  private double acAngle;
  // The change in acAngle at the current freqency. (radians)
  private double deltaAngle;
  // The change in acAngle that occurred the last time stepInTime was called.
  // (radians)
  private double stepAngle;

  // ----------------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------------

  /**
   * Sole constructor.
   */
  public ACPowerSupply() {
    super();
    this.maxAmplitude = 1.0; // biggest
    this.frequency = 1.0; // fastest
    this.acAngle = 0.0; // radians
    this.deltaAngle = (2 * Math.PI * this.frequency) / MIN_STEPS_PER_CYCLE; // radians
    this.stepAngle = 0.0; // radians
  }

  // ----------------------------------------------------------------------------
  // Accessors
  // ----------------------------------------------------------------------------

  /**
   * Sets the maximum amplitude.
   * 
   * @param maxAmplitude
   *          the maximum amplitude, 0...1 inclusive
   */
  public void setMaxAmplitude(double maxAmplitude) {
    assert (maxAmplitude >= 0 && maxAmplitude <= 1);
    this.maxAmplitude = maxAmplitude;
  }

  /**
   * Gets the maximum amplitude.
   * 
   * @return the maximum amplitude, 0...1 inclusive
   */
  public double getMaxAmplitude() {
    return this.maxAmplitude;
  }

  /**
   * Sets the frequency.
   * 
   * @param frequency
   *          the frequency, 0...1 inclusive
   */
  public void setFrequency(double frequency) {
    assert (frequency >= 0 && frequency <= 1);
    this.frequency = frequency;
    this.acAngle = 0.0;
    this.deltaAngle = (2 * Math.PI * this.frequency) / MIN_STEPS_PER_CYCLE;
  }

  /**
   * Gets the frequency.
   * 
   * @return the frequency, 0...1 inclusive
   */
  public double getFrequency() {
    return this.frequency;
  }

  /**
   * Change in acAngle the last time that stepInTime was called (ie, the last time
   * that the simulation clock ticked).
   * 
   * @return the acAngle, in radians
   */
  public double getStepAngle() {
    return this.stepAngle;
  }

  /*
   * Varies the amplitude over time, based on maxAmplitude and frequency.
   * Guaranteed to hit all peaks and zero crossings.
   */
  public void stepInTime(double dt) {
    if (this.maxAmplitude == 0) {
      setAmplitude(0.0);
    } else {
      double previousAngle = this.acAngle;

      // Compute the acAngle.
      this.acAngle += (dt * this.deltaAngle);

      // The actual change in acAngle on this tick of the simulation clock.
      this.stepAngle = this.acAngle - previousAngle;

      // Limit the acAngle to 360 degrees.
      if (this.acAngle >= 2 * Math.PI) {
        this.acAngle = this.acAngle % (2 * Math.PI);
      }

      // Calculate and set the amplitude.
      setAmplitude(this.maxAmplitude * Math.sin(this.acAngle));
    }
  }
}
