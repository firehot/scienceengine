// Copyright 2002-2011, University of Colorado

package com.mazalearn.scienceengine.experiments.electromagnetism.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

/**
 * Electromagnet is the model of an electromagnet.
 * The shape of the model is a circle,
 * and the calculation of the magnetic field at some point of interest varies
 * depending on whether the point is inside or outside the circle.
 * For 2D checking of containment, the shape behaves like a square ???
 * 
 * @author sridhar
 */
public class Electromagnet extends AbstractMagnet {

  private float maxStrengthOutside; // for debugging

  private SourceCoil sourceCoil;
  private AbstractCurrentSource currentSource;
  public static final int ELECTROMAGNET_LOOPS_MAX = 4;

  /**
   * Sole constructor.
   * 
   * @param sourceCoil
   *          the electromagnet's coil
   * @param currentSource
   *          the electromagnet's current source
   */
  public Electromagnet(String name, EMField emField, SourceCoil sourceCoil,
      AbstractCurrentSource currentSource, float x, float y, float angle) {
    super(ComponentType.ElectroMagnet, name, emField, x, y, angle);
    this.sourceCoil = sourceCoil;
    this.setSize(16, 16);
    this.maxStrengthOutside = 0.0f;
    this.setCurrentSource(currentSource);
    FixtureDef fixtureDef = new FixtureDef();
    PolygonShape rectangleShape = new PolygonShape();
    rectangleShape.setAsBox(getWidth()/2, getHeight()/2);
    fixtureDef.density = 1;
    fixtureDef.shape = rectangleShape;
    fixtureDef.filter.categoryBits = 0x0000;
    fixtureDef.filter.maskBits = 0x0000;
    this.createFixture(fixtureDef);
    initializeConfigs();
  }

  public void initializeConfigs() {
 /*   configs.add(new AbstractModelConfig<Float>(getName() + " Strength", 
        "Strength of magnet", 0f, 10000f) {
      public Float getValue() { return getStrength(); }
      public void setValue(Float value) { setStrength(value); }
      public boolean isPossible() { return isActive(); }
    });
    configs.add(new AbstractModelConfig<String>(getName() + " Flip Polarity", 
        "Direction of North Pole") {
      public void doCommand() { flipPolarity(); }
      public boolean isPossible() { return isActive(); }
    }); */
  }

  /**
   * Sets the electromagnet's current source.
   * 
   * @param currentSource
   */
  public void setCurrentSource(AbstractCurrentSource currentSource) {
    this.currentSource = currentSource;
    /*
     * The magnet size is a circle that has the same radius as the coil. Adding
     * half the wire width makes it look a little better.
     */
    double diameter = 2 * this.sourceCoil.getRadius();
        //+ (this.sourceCoil.getWireWidth() / 2);
    super.setSize((float) diameter, (float) diameter);
    
    // Current amplitude is proportional to amplitude of the current source.
    this.sourceCoil.setCurrentAmplitude(this.currentSource.getAmplitude());
    
    // Compute the electromagnet's emf amplitude.
    float amplitude = (this.sourceCoil.getNumberOfLoops() / (float) ELECTROMAGNET_LOOPS_MAX)
        * this.currentSource.getAmplitude();
    amplitude = Clamp.clamp(-1f, amplitude, 1f);
    
    /*
     * Set the strength. This is a bit of a "fudge". We set the strength of the
     * magnet to be proportional to its emf.
     */
    float strength = Math.abs(amplitude) * 10000f;
    setStrength(strength);
  }

  /**
   * Gets the eletromagnet's current source.
   * 
   * @return the current source
   */
  public AbstractCurrentSource getCurrentSource() {
    return this.currentSource;
  }

  /**
   * Is the specified point inside the magnet?
   * 
   * @param p
   * @return true iff p is contained inside magnet's area
   */
  public boolean isInside(Vector2 p) {
    float x = p.x / 5;
    float y = p.y / 5;
    return x >= -getWidth()/2 && x <= getWidth()/2 &&
        y >= -getHeight()/2 && y <= getHeight()/2;
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
   *   <li>origin is the center of the coil, at (0,0) 
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
    // Elemental terms
    double x = p.x;
    double y = p.y;
    double r = Math.sqrt((x * x) + (y * y));
    double R = getWidth() / 2;
    double distanceExponent = 3;

    /*
     * Inside the magnet, Bx = magnet strength = (2 * m) / (R^3). Rewriting this
     * gives us m = (magnet strength) * (R^3) / 2.
     */
    double m = getStrength() * Math.pow(R, distanceExponent) / 2;

    // Recurring terms
    double C1 = m / Math.pow(r, distanceExponent);
    double cosTheta = x / r;
    double sinTheta = y / r;

    // B-field component vectors
    float Bx = (float) (C1 * ((3 * cosTheta * cosTheta) - 1));
    float By = (float) (C1 * (3 * cosTheta * sinTheta));

    // B-field vector
    outputVector.set(Bx, By);

    // Use this to calibrate.
    if (outputVector.len() > this.maxStrengthOutside) {
      this.maxStrengthOutside = outputVector.len();
    }
  }
}