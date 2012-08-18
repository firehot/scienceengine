// Copyright 2002-2011, University of Colorado

package com.mazalearn.scienceengine.experiments.model.electromagnetism;

/**
 * ACPowerSupply is the model of an AC Power Supply.
 * <p>
 * The AC Power Supply has a configurable maximum voltage. A client varies the
 * maximum voltage amplitude and frequency. The voltage amplitude varies over
 * time.
 * 
 * @author Chris Malley (cmalley@pixelzoom.com)
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
  private double angle;
  // The change in angle at the current freqency. (radians)
  private double deltaAngle;
  // The change in angle that occurred the last time stepInTime was called.
  // (radians)
  private double stepAngle;
  private boolean enabled = true;

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
    this.angle = 0.0; // radians
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
    this.angle = 0.0;
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
   * Change in angle the last time that stepInTime was called (ie, the last time
   * that the simulation clock ticked).
   * 
   * @return the angle, in radians
   */
  public double getStepAngle() {
    return this.stepAngle;
  }

  // ----------------------------------------------------------------------------
  // ModelElement implementation
  // ----------------------------------------------------------------------------

  /*
   * Varies the amplitude over time, based on maxAmplitude and frequency.
   * Guaranteed to hit all peaks and zero crossings.
   * 
   * @see edu.colorado.phet.common.model.ModelElement#stepInTime(double)
   */
  public void stepInTime(double dt) {
    if (enabled) {

      if (this.maxAmplitude == 0) {
        setAmplitude(0.0);
      } else {
        double previousAngle = this.angle;

        // Compute the angle.
        this.angle += (dt * this.deltaAngle);

        // The actual change in angle on this tick of the simulation clock.
        this.stepAngle = this.angle - previousAngle;

        // Limit the angle to 360 degrees.
        if (this.angle >= 2 * Math.PI) {
          this.angle = this.angle % (2 * Math.PI);
        }

        // Calculate and set the amplitude.
        setAmplitude(this.maxAmplitude * Math.sin(this.angle));
      }
    }
  }
}
