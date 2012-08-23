// Copyright 2002-2012, University of Colorado

package com.mazalearn.scienceengine.experiments.model.electromagnetism;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.mazalearn.scienceengine.box2d.ScienceBody;

/**
 * AbstractMagnet is the abstract base class for all magnets.
 * 
 * @author sridhar
 */
public abstract class AbstractMagnet extends ScienceBody 
       implements EMField.IProducer {

  // ----------------------------------------------------------------------------
  // Instance data
  // ----------------------------------------------------------------------------

  private float width, height;
  private double strength;
  private double maxStrength;
  private double minStrength;

  // ----------------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------------

  /**
   * Sole constructor
   * @param  emField - Electromagnetic field to which magnet is coupled
   */
  public AbstractMagnet(EMField emField) {
    super();
    emField.registerProducer(this);
    
    this.width = 64;
    this.height = 8;
    PolygonShape magnetShape = new PolygonShape();
    magnetShape.setAsBox(this.width, this.height);
    this.createFixture(magnetShape, 1f);

    this.strength = 1.0;
    this.minStrength = 0.0; // couldn't be any weaker
    this.maxStrength = Double.POSITIVE_INFINITY; // couldn't be any stronger
  }

  // ----------------------------------------------------------------------------
  // Accessors
  // ----------------------------------------------------------------------------

  /**
   * Flips the magnet's polarity by rotating it 180 degrees.
   */
  public void flipPolarity() {
    setPositionAndAngle(getPosition().x, getPosition().y, 
        (float) ((getAngle() + Math.PI) % (2 * Math.PI)));
  }

  /**
   * Sets the maximum magnet strength. This value is used in rescaling of field
   * strength.
   * 
   * @param maxStrength
   *          the maximum strength, in Gauss
   */
  public void setMaxStrength(double maxStrength) {
    this.maxStrength = maxStrength;
    if (this.strength > this.maxStrength) {
      this.strength = this.maxStrength;
    }
    if (this.maxStrength < this.minStrength) {
      this.minStrength = this.maxStrength;
    }
  }

  /**
   * Gets the maximum magnet strength. This value is used in rescaling of field
   * strength.
   * 
   * @return the maximumum strength, in Gauss
   */
  public double getMaxStrength() {
    return this.maxStrength;
  }

  /**
   * Sets the minimum magnet strength. This value is used in rescaling of field
   * strength.
   * 
   * @param minStrength
   *          the minimum strength, in Gauss
   */
  public void setMinStrength(double minStrength) {
    this.minStrength = minStrength;
    if (this.strength < this.minStrength) {
      this.strength = this.minStrength;
    }
    if (this.minStrength > this.maxStrength) {
      this.maxStrength = this.minStrength;
    }
  }

  /**
   * Gets the minimum magnet strength. This value is used in rescaling of field
   * strength.
   * 
   * @return the minimum strength, in Gauss
   */
  public double getMinStrength() {
    return this.minStrength;
  }

  /**
   * Sets the magnitude of the magnet's strength, in Gauss.
   * 
   * @param strength
   *          the strength
   * @throws IllegalArgumentException
   *           if strength is outside of the min/max range
   */
  public void setStrength(double strength) {
    if (strength < this.minStrength || strength > this.maxStrength) {
      throw new IllegalArgumentException("strength out of range: " + strength);
    }
    this.strength = strength;
  }

  /**
   * Gets the magnitude of the magnet's strength, in Gauss.
   * 
   * @return the strength
   */
  public double getStrength() {
    return this.strength;
  }

  /**
   * Gets the B-field vector at a point in the global 2D space.
   * 
   * @param p - the point
   * @param outputVector - B-field is written here if provided, may be null
   * @return the B-field vector, outputVector if it was provided
   */
  public Vector2 getBField(final Vector2 p, Vector2 outputVector) {
    /*
     * Our models are based a magnet located at the origin, with the north pole
     * pointing down the positive x-axis. The point we receive is in global 2D
     * space. So transform the point to the magnet's local coordinate system,
     * adjusting for position and orientation.
     */
    Vector2 localPoint = this.getLocalPoint(p);
    // get strength in magnet's local coordinate frame
    getBFieldRelative(localPoint, outputVector);

    // Adjust the field vector to match the magnet's angle.
    outputVector.rotate(getAngle());

    // Clamp magnitude to magnet strength.
    // TODO: why do we need to do this?
    double magnetStrength = getStrength();
    double magnitude = outputVector.len();
    if (magnitude > magnetStrength) {
      outputVector.x = (float) (outputVector.x * magnetStrength / magnitude);
      outputVector.y = (float) (outputVector.y * magnetStrength / magnitude);
    }

    return outputVector;
  }

  /**
   * Gets the B-field vector at a point in the magnet's local 2D coordinate
   * frame. That is, the point is relative to the magnet's origin. In the
   * magnet's local 2D coordinate frame, it is located at (0,0), and its north
   * pole is pointing down the positive x-axis.
   * 
   * @param p
   *          the point
   * @param outputVector
   *          B-field is written here if provided, may NOT be null
   * @return outputVector
   */
  protected abstract Vector2 getBFieldRelative(final Vector2 p,
      Vector2 outputVector);

  /**
   * Sets the physical size of the magnet.
   * 
   * @param width -  the width
   * @param height - the height
   * @throws IllegalArgumentException if width or height is <= 0
   */
  public void setSize(float width, float height) {
    if (width <= 0 || height <= 0) {
      throw new IllegalArgumentException("dimensions must be > 0");
    }
    this.width = width;
    this.height = height;
  }

  /**
   * Gets the physical width of the magnet.
   * 
   * @return the width
   */
  public float getWidth() {
    return this.width;
  }

  /**
   * Gets the physical height of the magnet.
   * 
   * @return the height
   */
  public float getHeight() {
    return this.height;
  }
}
