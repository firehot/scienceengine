// Copyright 2002-2012, University of Colorado

package com.mazalearn.scienceengine.experiments.electromagnetism.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;

/**
 * CoilMagnet is the model of a coil magnet. The shape of the model is a circle,
 * and the calculation of the magnetic field at some point of interest varies
 * depending on whether the point is inside or outside the circle.
 * For 2D checking of containment, the shape behaves like a square ???
 * 
 * @author sridhar
 */
public abstract class CoilMagnet extends AbstractMagnet {

  // ----------------------------------------------------------------------------
  // Instance data
  // ----------------------------------------------------------------------------

  private double maxStrengthOutside; // for debugging
  
  // ----------------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------------

  public CoilMagnet(String name, EMField emField, float x, float y, float angle) {
    super(name, emField, y, angle, x);
    this.maxStrengthOutside = 0.0;
  }

  // ----------------------------------------------------------------------------
  // Accessors
  // ----------------------------------------------------------------------------

  /**
   * Is the specified point inside the magnet?
   * 
   * @param p
   * @return true iff p is contained inside magnet's area
   */
  public boolean isInside(Vector2 p) {
    // Test all the fixtures of the magnet to check for containment of p.
    for (Fixture f: this.getFixtureList()) {
      if (f.testPoint(p)) return true;
    }
    return false;
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
   * Gets the B-field vector for points inside the coil. <p> Inside the coil (r
   * <= R) : <ul> <li>Bx = ( 2 * m ) / R^e = magnet strength <li>By = 0 </ul>
   * 
   * @param p
   * @param outputVector
   */
  private void getBFieldInside(Vector2 p, Vector2 outputVector /* output */) {
    outputVector.set((float) getStrength(), 0f);
  }

  /*
   * Gets the B-field vector for points outside the coil. <p> Algorithm courtesy
   * of Mike Dubson (dubson@spot.colorado.edu). <p> Terminology: <ul> <li>axes
   * oriented with +X right, +Y up <li>origin is the center of the coil, at
   * (0,0) <li>(x,y) is the point of interest where we are measuring the
   * magnetic field <li>C = a fudge factor, set so that the lightbulb will light
   * <li>m = magnetic moment = C * #loops * current in the coil <li>R = radius
   * of the coil <li>r = distance from the origin to (x,y) <li>theta = angle
   * between the X axis and (x,y) <li>Bx = X component of the B field <li>By = Y
   * component of the B field <li>e is the exponent that specifies how the field
   * decreases with distance (3 in reality) </ul> <p> Outside the coil (r > R) :
   * <ul> <li>Bx = ( m / r^e ) * ( ( 3 * cos(theta) * cos(theta) ) - 1 ) <li>By
   * = ( m / r^e ) * ( 3 * cos(theta) * sin(theta) ) </ul> <br>where: <ul> <li>r
   * = sqrt( x^2 + y^2 ) <li>cos(theta) = x / r <li>sin(theta) = y / r </ul>
   * 
   * @param p
   * 
   * @param outputVector
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
