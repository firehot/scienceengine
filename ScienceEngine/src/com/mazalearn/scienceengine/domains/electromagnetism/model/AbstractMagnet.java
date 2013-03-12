// Copyright 2002-2012, University of Colorado

package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.core.model.IMagneticField;
import com.mazalearn.scienceengine.core.model.Science2DBody;

/**
 * AbstractMagnet is the abstract base class for all magnets.
 * 
 * @author sridhar
 */
public abstract class AbstractMagnet extends Science2DBody 
       implements IMagneticField.Producer {

  private static final float TOLERANCE = 0.1f;
  private float width, height;
  private float strength;

  /**
   * Sole constructor
   * @param  emField - Electromagnetic field to which magnet is coupled
   */
  public AbstractMagnet(ComponentType componentType, float x, float y, float angle) {
    super(componentType, x, y, angle);
    this.strength = 1.0f;
  }
  
  /**
   * Sets the magnitude of the magnet's strength, in Gauss.
   * 
   * @param strength
   *          the strength
   * @throws IllegalArgumentException
   *           if strength is outside of the min/max range
   */
  public void setStrength(float strength) {
    if (Math.abs(this.strength - strength) > TOLERANCE) {
      this.strength = strength;
      if (getModel() != null) {
        getModel().notifyFieldChange();
      }
    }
  }

  public void setAngle(float angle) {
    if (Math.abs(getAngle() - angle) > TOLERANCE) {
       setPositionAndAngle(getPosition(), angle);
       if (getModel() != null) {
         getModel().notifyFieldChange();
       }
    }
  }

  /**
   * Gets the magnitude of the magnet's strength, in Gauss.
   * 
   * @return the strength
   */
  public float getStrength() {
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
    outputVector.rotate(getAngle() * MathUtils.radiansToDegrees);

    // Clamp magnitude to magnet strength.
    // TODO: why do we need to do this?
    float magnetStrength = getStrength();
    if (outputVector.len() > Math.abs(magnetStrength)) {
      outputVector.nor().mul(Math.abs(magnetStrength));
    }

    return outputVector;
  }

  /**
   * Gets the B-field vector at a point in the magnet's local 2D coordinate
   * frame. That is, the point is relative to the magnet's origin. In the
   * magnet's local 2D coordinate frame, it is located at (0,0), and its north
   * pole is pointing down the positive x-axis.
   * 
   * @param p - the point
   * @param outputVector - B-field is written here if provided, may NOT be null
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
    this.width = width;
    this.height = height;
  }

  protected void scaleSize(float scale) {
    this.height *= scale;
    this.width *= scale;
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
