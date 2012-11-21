// Copyright 2002-2012, University of Colorado

package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.model.ICurrent;
import com.mazalearn.scienceengine.core.model.Science2DBody;

/**
 * PickupCoil is the model of a pickup coil. Its behavior follows Faraday's Law
 * for electromagnetic induction.
 * 
 * @author Chris Malley (cmalley@pixelzoom.com)
 * @author sridhar
 */
public class PickupCoil extends Science2DBody implements ICurrent.Source {

  private static final boolean DEBUG_CALIBRATION = false;
  private static final float MIN_PICKUP_LOOP_RADIUS = 68.0f;
  private static final int NUM_SAMPLE_POINTS = 9;
  private static final float TOLERANCE = 0.01f;

  private float averageBx; // in Gauss
  private float flux; // in webers
  private float emf; // in volts
  private float biggestAbsEmf; // in volts
  private Vector2 samplePoints[]; // B-field sample points
  private float calibrationEmf;

  private Vector2 sampleBField;
  private int numberOfLoops;
  private float wireWidth;
  private float current;
  // Radius of all loops in the coil.
  private float radius;
  // Terminals
  private Vector2 firstTerminal = new Vector2(), secondTerminal = new Vector2();

  /**
   * Constructs a PickupCoil that uses a fixed number of sample points to
   * measure the magnet's B-field.
   * Creates a default coil with one loop, radius=10,
   * wireWidth=16, loopSpacing=25
   */
  public PickupCoil(float x, float y, float angle, float calibrationEmf) {
    this(ComponentType.PickupCoil, x, y, angle, 1, 8, 16, 25);

    assert (calibrationEmf >= 1);
    this.calibrationEmf = calibrationEmf;

    createSamplePoints();

    this.averageBx = 0f;
    this.flux = 0.0f;
    this.emf = 0.0f;
    this.biggestAbsEmf = 0.0f;

    // Reusable objects
    this.sampleBField = new Vector2();
  }

  /**
   * Fully-specified constructor.
   * 
   * @param numberOfLoops -  number of loops in the coil
   * @param radius - radius used for all loops
   * @param wireWidth - width of the wire
   * @param loopSpacing - space between the loops
   */
  private PickupCoil(ComponentType componentType, float x, float y, float angle, 
      int numberOfLoops, float radius, float wireWidth, float loopSpacing) {
    super(componentType, x, y, angle);
    this.numberOfLoops = numberOfLoops;
    this.radius = radius;
    this.wireWidth = wireWidth;
    this.current = 0f;
    FixtureDef fixtureDef = new FixtureDef();
    // This is 2D - so we model the coil as being perpendicular to the plane
    // and only the intersections at top and bottom with plane are fixtures
    Vector2 pos = new Vector2();
    PolygonShape rectangleShape = new PolygonShape();
    pos.set(0, radius);
    rectangleShape.setAsBox(this.wireWidth/2, this.wireWidth/2, pos, 0);
    fixtureDef.density = 1;
    fixtureDef.shape = rectangleShape;
    fixtureDef.filter.categoryBits = 0x0000;
    fixtureDef.filter.maskBits = 0x0000;
    this.createFixture(fixtureDef);
    pos.set(0, -radius);
    rectangleShape.setAsBox(this.wireWidth/2, this.wireWidth/2, pos, 0);
    this.createFixture(fixtureDef);
    rectangleShape.dispose();
  }
  
  @Override
  public void initializeConfigs() {
    configs.add(new AbstractModelConfig<Float>(getName() + " Coil Loops", 
        "Number of loops of coil", 1f, 4f) {
      public Float getValue() { return getNumberOfLoops(); }
      public void setValue(Float value) { setNumberOfLoops(value); }
      public boolean isPossible() { return isActive(); }
    });
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
        radius / numberOfSamplePointsOnRadius;

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
    float deltaFlux = flux - this.flux;
    this.flux = flux;
    
    // Induced emf.
    float emf = -(deltaFlux / dt);
    
    // If the emf has changed, set the current in the coil and notify observers.
    if (emf != this.emf) {
      this.emf = emf;
    
      // Current is proportional to emf
      float current = Clamp.clamp(-2.5f, emf / this.calibrationEmf, +2.5f);
      setCurrent(current);
    }
    
    if (ScienceEngine.DEV_MODE == DevMode.DEBUG) {
      calibrateEmf();
    }
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

    // Sum the B-field sample points.
    float sumBx = 0;
    for (int i = 0; i < this.samplePoints.length; i++) {
      // Translate to global coordinates from local
      Vector2 globalPoint = this.getWorldPoint(this.samplePoints[i]);
      // Find the B-field vector at that point.
      getModel().getBField(globalPoint, this.sampleBField /* output */);

      /*
       * If the B-field x component is equal to the magnet strength, then our
       * B-field sample was inside the magnet. Use the fudge factor to scale the
       * sample so that the transitions between inside and outside are not
       * abrupt. See Unfuddle #248.
       */
      float Bx = this.sampleBField.x;

      // Accumulate a sum of the sample points.
      sumBx += Bx;
    }

    return sumBx;
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
    float height = 2 * radius;
    return width * height;
  }

  /**
   * Sets the number of loops in the coil. This method destroys any existing
   * loops and creates a new set.
   * 
   * @param numberOfLoops
   *          the number of loops - must be > 0
   */
  public void setNumberOfLoops(float numberOfLoops) {
    this.numberOfLoops = Math.round(numberOfLoops);
  }

  /**
   * Gets the number of loops in the coil.
   * 
   * @return the number of loops
   */
  public float getNumberOfLoops() {
    return this.numberOfLoops;
  }

  private void setCurrent(float current) {
    if (Math.abs(this.current - current) > TOLERANCE) {
      this.current = current;
      getModel().notifyCurrentChange(this);
    }
  }

  /**
   * Gets the current in the coil, in amperes
   */
  public float getCurrent() {
    return this.current;
  }
  
  @Override
  public Vector2 getT1Position() {
    return firstTerminal.set(getPosition()).add(-4.5f, 12f);
  }

  @Override
  public Vector2 getT2Position() {
    return secondTerminal.set(getPosition()).add(-4.5f, 13.5f);
  }
  
}