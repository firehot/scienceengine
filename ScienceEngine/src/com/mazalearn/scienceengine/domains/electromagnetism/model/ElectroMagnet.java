// Copyright 2002-2011, University of Colorado

package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.model.ICurrent;

/**
 * Electromagnet is the model of an electromagnet.
 * The shape of the model is a circle,
 * and the calculation of the magnetic field at some point of interest varies
 * depending on whether the point is inside or outside the circle.
 * For 2D checking of containment, the shape behaves like a square ???
 * 
 * @author sridhar
 */
public class ElectroMagnet extends AbstractMagnet implements ICurrent.Sink {

  private static final float OUTPUT_SCALE = 200f;
  private static final float TOLERANCE = 0.1f;
  private static final int ELECTROMAGNET_LOOPS_MAX = 4;
  private static final float MAX_EMF = 25;
  public static final float DISPLAY_WIDTH = 38f;
  private static final float COIL_WIDTH = DISPLAY_WIDTH / ScreenComponent.PIXELS_PER_M;

  private float maxStrengthOutside; // for debugging

  // Current flowing in coil
  private float current = 0f;
  // Number of loops in the coil.
  private int numberOfLoops;
  // Terminals
  private Vector2 firstTerminal = new Vector2(), secondTerminal = new Vector2();


  /**
   * Sole constructor.
   */
  public ElectroMagnet(float x, float y, float angle) {
    super(ComponentType.ElectroMagnet, x, y, angle);
    this.numberOfLoops = 1;
    // Modeled as a square with diameter equal to side of square
    this.setSize(16, 16);
    this.maxStrengthOutside = 0.0f;
    FixtureDef fixtureDef = new FixtureDef();
    PolygonShape rectangleShape = new PolygonShape();
    rectangleShape.setAsBox(getWidth()/2, getHeight()/2);
    fixtureDef.density = 1;
    fixtureDef.shape = rectangleShape;
    fixtureDef.filter.categoryBits = 0x0000;
    fixtureDef.filter.maskBits = 0x0000;
    this.createFixture(fixtureDef);
    rectangleShape.dispose();
  }

  @Override
  public void initializeConfigs() {
    super.initializeConfigs();
    configs.add(new AbstractModelConfig<Float>(this, 
        Parameter.CoilLoops, 1f, 4f) {
      public Float getValue() { return getNumberOfLoops(); }
      public void setValue(Float value) { setNumberOfLoops(value); }
      public boolean isPossible() { return isActive(); }
    });
  }

  public void setCurrent(float current) {
    if (Math.abs(this.current - current) < TOLERANCE) {
      return;
    }
    this.current = current;
    updateStrength();
  }
  
  private void updateStrength() {
    // Compute the electromagnet's emf amplitude.
    float emf = (numberOfLoops / (float) ELECTROMAGNET_LOOPS_MAX) * current;
    emf = Clamp.clamp(-MAX_EMF, emf, MAX_EMF);
    
    /*
     * Set the strength. This is a bit of a "fudge". We set the strength of the
     * magnet to be proportional to its emf.
     */
    float strength = emf * OUTPUT_SCALE;
    setStrength(strength);
  }

  /**
   * Is the specified point inside the magnet?
   * 
   * @param p
   * @return true iff p is contained inside magnet's area
   */
  public boolean isInside(Vector2 p) {
    return p.x >= -(getWidth()/2 + numberOfLoops * COIL_WIDTH) && p.x <= getWidth()/2 &&
        p.y >= -getHeight()/2 && p.y <= getHeight()/2;
  }

  // ----------------------------------------------------------------------------
  // AbstractMagnet implementation
  // ----------------------------------------------------------------------------

  /**
   * Gets the B-field vector at a point in the magnet's local 2D coordinate
   * frame.
   * 
   * @param p
   * @param outputVector
   * @return outputVector
   */
  protected Vector2 getBFieldRelative(Vector2 p, Vector2 outputVector) {
    // Algorithm differs depending on whether we're inside or outside the shape
    // that defines the coil.
    if (isInside(p)) {
      getBFieldInside(p, outputVector);
    } else {
      getBFieldOutside(p, outputVector);
    }

    return outputVector;
  }

  /*
   * Gets the B-field vector for points inside the coil. 
   * <p> Inside the coil (r <= R) : 
   * <ul> 
   *   <li>Bx = ( 2 * m ) / R^e = magnet strength 
   *   <li>By = 0 
   * </ul>
   * 
   * @param p - the point
   * @param outputVector - bfield
   */
  private void getBFieldInside(Vector2 p, Vector2 outputVector /* output */) {
    outputVector.set((float) getStrength(), 0f);
  }

  /*
   * Gets the B-field vector for points outside the coil. 
   * <p> Algorithm courtesy of Mike Dubson (dubson@spot.colorado.edu). 
   * <p> Terminology: 
   * <ul> 
   *   <li>axes oriented with +X right, +Y up 
   *   <li>origin is the center of the coil, at (0,0) including number of loops
   *   <li>(x,y) is the point of interest where we are measuring the magnetic field 
   *   <li>C = a fudge factor, set so that the lightbulb will light 
   *   <li>m = magnetic moment = C * #loops * current in the coil 
   *   <li>R = radius of the coil
   *   <li>r = distance from the origin to (x,y) 
   *   <li>theta = angle between the X axis and (x,y)
   *   <li>Bx = X component of the B field 
   *   <li>By = Y component of the B field 
   *   <li>e is the exponent that specifies how the field decreases with distance (3 in reality) 
   * </ul> 
   * <p> Outside the coil (r > R) :
   * <ul> 
   *   <li>Bx = ( m / r^e ) * ( ( 3 * cos(theta) * cos(theta) ) - 1 ) 
   *   <li>By = ( m / r^e ) * ( 3 * cos(theta) * sin(theta) ) 
   * </ul> 
   * <br>where: 
   * <ul> 
   *   <li>r = sqrt( x^2 + y^2 ) 
   *   <li>cos(theta) = x / r 
   *   <li>sin(theta) = y / r 
   * </ul>
   * 
   * @param p - the point
   * @param outputVector - bfield
   */
  private void getBFieldOutside(Vector2 p, Vector2 outputVector /* output */) {
    // x,y are relative to Electromagnet at body position with 0 loops i.e. width/2, height/2.
    // Adjust for number of loops - origin is at width/2 - #loops * coilwidth/2
    
    float x = p.x - numberOfLoops * COIL_WIDTH;
    float y = p.y;
    float r = (float) Math.sqrt((x * x) + (y * y));
    float R = getWidth() / 2;
    float distanceExponent = 3;

    /*
     * Inside the magnet, Bx = magnet strength = (2 * m) / (R^3). Rewriting this
     * gives us m = (magnet strength) * (R^3) / 2.
     */
    float m = (float) (getStrength() * Math.pow(R, distanceExponent) / 2);

    // Recurring terms
    // Fudge factor of 1 below as multiple
    float C1 = (float) (1 * m / Math.pow(r, distanceExponent));
    float cosTheta = x / r;
    float sinTheta = y / r;

    // B-field component vectors
    float Bx = C1 * ((3 * cosTheta * cosTheta) - 1);
    float By = C1 * (3 * cosTheta * sinTheta);

    // B-field vector
    outputVector.set(Bx, By);

    // Use this to calibrate.
    if (outputVector.len() > this.maxStrengthOutside) {
      this.maxStrengthOutside = outputVector.len();
    }
  }

  public float getNumberOfLoops() {
    return numberOfLoops;
  }

  public void setNumberOfLoops(float numberOfLoops) {
    if (this.numberOfLoops != Math.round(numberOfLoops)) {
      this.numberOfLoops = Math.round(numberOfLoops);
      updateStrength();
      getModel().notifyFieldChange();
    }
  }

  public float getCoilWidth() {
    return COIL_WIDTH;
  }

  @Override
  public Vector2 getT1Position() {
    return firstTerminal.set(getPosition())
        .add(ScreenComponent.getScaledX(1f), ScreenComponent.getScaledY(-2f));
  }

  @Override
  public Vector2 getT2Position() {
    return secondTerminal.set(getPosition())
        .add(ScreenComponent.getScaledX(1.5f), ScreenComponent.getScaledY(3.5f));
  }
  
}