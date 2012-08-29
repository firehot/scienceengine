// Copyright 2002-2012, University of Colorado

package com.mazalearn.scienceengine.experiments.electromagnetism.model;

import com.badlogic.gdx.math.Vector2;

/**
 * PickupCoil is the model of a pickup coil. Its behavior follows Faraday's Law
 * for electromagnetic induction.
 * 
 * @author Chris Malley (cmalley@pixelzoom.com)
 * @author sridhar
 */
public class PickupCoil extends AbstractCoil {

  // ----------------------------------------------------------------------------
  // Class data
  // ----------------------------------------------------------------------------

  private static final boolean DEBUG_CALIBRATION = false;
  public static final float MIN_PICKUP_LOOP_RADIUS = 68.0f;
  private static final int NUM_SAMPLE_POINTS = 9;

  private EMField emField;

  private float averageBx; // in Gauss
  private float flux; // in webers
  private float deltaFlux; // in webers
  private float emf; // in volts
  private float biggestAbsEmf; // in volts
  private Vector2 samplePoints[]; // B-field sample points
  private float transitionSmoothingScale;
  private float calibrationEmf;

  // Reusable objects
  private Vector2 sampleBField;

  /**
   * Constructs a PickupCoil that uses a fixed number of sample points to
   * measure the magnet's B-field.
   * 
   * @param emField
   */
  public PickupCoil(EMField emField, float calibrationEmf) {
    super("PickupCoil");

    assert (emField != null);
    this.emField = emField;

    assert (calibrationEmf >= 1);
    this.calibrationEmf = calibrationEmf;

    createSamplePoints();

    this.averageBx = 0f;
    this.flux = 0.0f;
    this.deltaFlux = 0.0f;
    this.emf = 0.0f;
    this.biggestAbsEmf = 0.0f;
    this.transitionSmoothingScale = 1.0f; // no smoothing

    // Reusable objects
    this.sampleBField = new Vector2();

    // loosely packed loops
    setLoopSpacing(1.5f * getWireWidth());
  }

  /**
   * Gets the magnetic flux.
   * 
   * @return the flux, in Webers
   */
  public double getFlux() {
    return this.flux;
  }

  /**
   * Gets the change in magnetic flux.
   * 
   * @return change in magnetic flux, in Webers
   */
  public double getDeltaFlux() {
    return this.deltaFlux;
  }

  /**
   * Gets the average Bx of the pickup coil's sample points.
   * 
   * @return
   */
  public float getAverageBx() {
    return this.averageBx;
  }

  /**
   * Gets the emf.
   * 
   * @return the emf
   */
  public double getEmf() {
    return this.emf;
  }

  /**
   * Gets the biggest emf that the pickup coil has seen.
   * 
   * @return the biggest emf
   */
  public double getBiggestEmf() {
    return this.biggestAbsEmf;
  }

  /**
   * Gets the sample points used to measure the B-field and calculate emf.
   * 
   * @return
   */
  public Vector2[] getSamplePoints() {
    return this.samplePoints;
  }

  /**
   * When the coil's radius changes, update the sample points.
   */
  public void setRadius(float radius) {
    super.setRadius(radius);
    createSamplePoints();
  }

  /**
   * Sets a scaling factor used to smooth out abrupt changes that occur when the
   * magnet transitions between being inside & outside the coil.
   * <p/>
   * This is used to scale the B-field for sample points inside the magnet,
   * eliminating abrupt transitions at the left and right edges of the magnet.
   * For any sample point inside the magnet, the B field sample is multiplied by
   * this value.
   * <p/>
   * To set this value, follow these steps:
   * <ol>
   * <li>enable the developer controls for "pickup transition scale" and
   * "display flux"
   * <li>move the magnet horizontally through the coil until, by moving it one
   * pixel, you see an abrupt change in the displayed flux value.
   * <li>note the 2 flux values when the abrupt change occurs
   * <li>move the magnet so that the larger of the 2 flux values is displayed
   * <li>adjust the developer control until the larger value is reduced to
   * approximately the same value as the smaller value.
   * </ol>
   * 
   * @param scale
   *          0 < scale <= 1
   */
  public void setTransitionSmoothingScale(float scale) {
    if (scale <= 0 || scale > 1) {
      throw new IllegalArgumentException("scale must be > 0 and <= 1: " + scale);
    }
    this.transitionSmoothingScale = scale;
    // no need to update, wait for next clock tick
  }

  /**
   * See setTransitionSmoothingScale.
   * 
   * @return
   */
  public double getTransitionSmoothingScale() {
    return this.transitionSmoothingScale;
  }

  /**
   * Dividing the coil's emf by this number will give us the coil's current
   * amplitude, a number between 0 and 1 that determines the responsiveness of
   * view components. This number should be set as close as possible to the
   * maximum emf that can be induced given the range of all model parameters.
   * <p/>
   * See PickupCoil.calibrateEmf for guidance on how to set this.
   * 
   * @param calibrationEmf
   */
  public void setCalibrationEmf(float calibrationEmf) {
    if (!(calibrationEmf >= 1)) {
      throw new IllegalArgumentException("calibrationEmf must be >= 1: "
          + calibrationEmf);
    }
    this.calibrationEmf = calibrationEmf;
    // no need to update, wait for next clock tick
  }

  public double getCalibrationEmf() {
    return this.calibrationEmf;
  }

  /**
   * A fixed number of points is distributed along a vertical line that goes
   * through the center of a pickup coil. The number of sample points must be
   * odd, so that one point is at the center. The points at the outer edge are
   * guaranteed to be on the coil.
   */
  public void createSamplePoints() {

    this.samplePoints = new Vector2[NUM_SAMPLE_POINTS];
    final double numberOfSamplePointsOnRadius = (NUM_SAMPLE_POINTS - 1) / 2;
    final double samplePointsYSpacing = 
        getRadius() / numberOfSamplePointsOnRadius;

    // all sample points share the same x offset
    final float xOffset = 0;

    // Center
    int index = 0;
    samplePoints[index++] = new Vector2(xOffset, 0);

    // Offsets below & above the center
    float y = 0;
    for (int i = 0; i < numberOfSamplePointsOnRadius; i++) {
      y += samplePointsYSpacing;
      samplePoints[index++] = new Vector2(xOffset, y);
      samplePoints[index++] = new Vector2(xOffset, -y);
    }
  }

  // ----------------------------------------------------------------------------
  // ModelElement implementation
  // ----------------------------------------------------------------------------

  /**
   * Handles ticks of the simulation clock. Calculates the induced emf using
   * Faraday's Law.
   * 
   * @param dt - time delta
   */
  public void singleStep(float dt) {
    // Sum the B-field sample points.
    float sumBx = getSumBx();
    
    // Average the B-field sample points.
    this.averageBx = sumBx / this.samplePoints.length;
    
    // Flux in one loop.
    float A = getEffectiveLoopArea();
    float loopFlux = A * this.averageBx;
    
    // Flux in the coil.
    float flux = getNumberOfLoops() * loopFlux;
    
    // Change in flux.
    this.deltaFlux = flux - this.flux;
    this.flux = flux;
    
    // Induced emf.
    float emf = -(this.deltaFlux / dt);
    
    // If the emf has changed, set the current in the coil and notify observers.
    if (emf != this.emf) {
      this.emf = emf;
    
      // Current amplitude is proportional to emf amplitude.
      float amplitude = Clamp.clamp(-1, emf / this.calibrationEmf, +1);
      setCurrentAmplitude(amplitude);
    }
    
    calibrateEmf();
  }

  /*
   * Provides assistance for calibrating this coil. The easiest way to calibrate
   * is to run the sim in developer mode, then follow these steps for each
   * module that has a pickup coil.
   * 
   * 1. Set the "Pickup calibration EMF" developer control to its smallest
   * value. 2. Set the model parameters to their maximums, so that maximum emf
   * will be generated. 3. Do whatever is required to generate emf (move magnet
   * through coil, run generator, etc.) 4. Watch System.out for a message that
   * tells you what value to use. 5. Change the value of the module's
   * CALIBRATION_EMF constant.
   */
  private void calibrateEmf() {

    double absEmf = Math.abs(this.emf);

    /*
     * Keeps track of the biggest emf seen by the pickup coil. This is useful
     * for determining the desired value of calibrationEmf. Set
     * DEBUG_CALIBRATION=true, run the sim, set model controls to their max
     * values, then observe this debug output. The largest value that you see is
     * what you should use for calibrationEmf.
     */
    if (absEmf > this.biggestAbsEmf) {
      this.biggestAbsEmf = this.emf;
      if (DEBUG_CALIBRATION) {
        System.out.println("PickupCoil.updateEmf: biggestEmf="
            + this.biggestAbsEmf);
      }

      /*
       * If this prints, you have calibrationEmf set too low. This will cause
       * view components to exhibit responses that are less then their maximums.
       * For example, the voltmeter won't fully deflect, and the lightbulb won't
       * fully light.
       */
      if (this.biggestAbsEmf > this.calibrationEmf) {
        System.out
            .println("PickupCoil.updateEmf: you should recalibrate module \""
                + "\" with CALIBRATION_EMF="
                + this.biggestAbsEmf);
      }
    }

    /*
     * TODO The coil could theoretically be self-calibrating. If we notice that
     * we've exceeded calibrationEmf, then adjust calibrationEmf. This would be
     * OK as long as we started with a value that was in the ballpark, because
     * we don't want the user to perceive a noticeable change in the sim's
     * behavior.
     */
  }

  /*
   * Gets the sum of Bx at the coil's sample points.
   */
  private float getSumBx() {

    //TODO ??? final double magnetStrength = this.emField.getStrength();

    // Sum the B-field sample points.
    double sumBx = 0;
    for (int i = 0; i < this.samplePoints.length; i++) {
      // Translate to global coordinates from local
      Vector2 globalPoint = this.getWorldPoint(this.samplePoints[i]);
      // Find the B-field vector at that point.
      this.emField.getBField(globalPoint, this.sampleBField /* output */);

      /*
       * If the B-field x component is equal to the magnet strength, then our
       * B-field sample was inside the magnet. Use the fudge factor to scale the
       * sample so that the transitions between inside and outside are not
       * abrupt. See Unfuddle #248.
       */
      double Bx = this.sampleBField.x;
 /*TODO ???     if (Math.abs(Bx) == magnetStrength) {
        Bx *= this.transitionSmoothingScale;
      }
*/
      // Accumulate a sum of the sample points.
      sumBx += Bx;
    }

    return (float) sumBx;
  }

  /*
   * See Unfuddle #721. When the magnet is in the center of the coil, increasing
   * the loop size should decrease the EMF. But since we are averaging sample
   * points on a vertical line, multiplying by the actual area would
   * (incorrectly) result in an EMF increase. The best solution would be to take
   * sample points across the entire coil, but that requires many changes, so
   * Mike Dubson came up with this workaround. By fudging the area using a thin
   * vertical rectangle, the results are qualitatively (but not quantitatively)
   * correct.
   * 
   * NOTE: This fix required recalibration of all the scaling factors accessible
   * via developer controls.
   */
  private float getEffectiveLoopArea() {
    float width = MIN_PICKUP_LOOP_RADIUS;
    float height = 2 * getRadius();
    return width * height;
  }
}