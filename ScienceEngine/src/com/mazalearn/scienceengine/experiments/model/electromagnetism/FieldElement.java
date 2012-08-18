
package com.mazalearn.scienceengine.experiments.model.electromagnetism;

import com.badlogic.gdx.math.Vector2;

/**
 * FieldElement has basic attributes for an element in an EM Field:
 * <ul>
 * <li>whether the object is enabled
 * <li>location
 * <li>direction
 * </ul>
 * 
 * @author sridhar
 */
public class FieldElement {

  // ----------------------------------------------------------------------------
  // Instance data
  // ----------------------------------------------------------------------------

  protected Vector2 location;
  protected float direction;
  protected boolean enabled;

  // ----------------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------------

  /**
   * Zero-argument constructor. The object is enabled, location is (0,0),
   * direction is 0.
   */
  public FieldElement() {
    this(new Vector2(0, 0), 0);
  }

  /**
   * Fully-specified constructor. The object is enabled, located and orientated
   * as specified.
   * 
   * @param location - the location
   * @param direction - the direction, in degrees
   */
  public FieldElement(Vector2 location, float direction) {
    super();
    assert (location != null);
    this.enabled = true;
    this.location = new Vector2(location);
    this.direction = direction;
  }

  // ----------------------------------------------------------------------------
  // Accessors
  // ----------------------------------------------------------------------------

  /**
   * Enabled/disables this object.
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * @return whether object is enabled
   */
  public boolean isEnabled() {
    return this.enabled;
  }

  /**
   * Sets the location in 2D space.
   * 
   * @param location - the location
   */
  public void setLocation(final Vector2 location) {
    this.location.set(location);
  }

  /**
   * Sets the location in 2D space.
   * 
   * @param x
   *          location X coordinate
   * @param y
   *          location Y coordinate
   */
  public void setLocation(float x, float y) {
    this.location.set(x, y);
  }

  /**
   * Gets the location in 2D space.
   * 
   * @param outputPoint - point into which the location should be written
   * @return the location
   */
  public Vector2 getLocation(Vector2 outputPoint /* output */) {
    if (outputPoint == null) {
      throw new IllegalArgumentException("Null output");
    } else {
      outputPoint.set(location.x, location.y);
    }
    return outputPoint;
  }

  /**
   * Sets the direction. Positive values indicate clockwise rotation.
   * 
   * @param direction - the direction, in radians
   */
  public void setDirection(float direction) {
    this.direction = direction;
  }

  /**
   * @return the direction, in radians.
   */
  public double getDirection() {
    return this.direction;
  }
}
