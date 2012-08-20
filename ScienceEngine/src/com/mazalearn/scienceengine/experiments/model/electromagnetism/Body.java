
package com.mazalearn.scienceengine.experiments.model.electromagnetism;

import com.badlogic.gdx.math.Vector2;

/**
 * Body has basic attributes for an element in an EM Field:
 * <ul>
 * <li>position
 * <li>angle
 * </ul>
 * 
 * @author sridhar
 */
public class Body {

  // ----------------------------------------------------------------------------
  // Instance data
  // ----------------------------------------------------------------------------

  protected Vector2 position;
  protected double angle;

  // ----------------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------------

  /**
   * Zero-argument constructor. The object is enabled, position is (0,0),
   * angle is 0.
   */
  public Body() {
    this(new Vector2(0, 0), 0);
  }

  /**
   * Fully-specified constructor. The object is enabled, located and orientated
   * as specified.
   * 
   * @param position - the position
   * @param angle - the angle, in degrees
   */
  public Body(Vector2 position, float angle) {
    super();
    assert (position != null);
    this.position = new Vector2(position);
    this.angle = angle;
  }

  // ----------------------------------------------------------------------------
  // Accessors
  // ----------------------------------------------------------------------------

  /**
   * Sets the position in 2D space.
   * 
   * @param position - the position
   */
  public void setPosition(final Vector2 position) {
    this.position.set(position);
  }

  /**
   * Sets the position in 2D space.
   * 
   * @param x
   *          position X coordinate
   * @param y
   *          position Y coordinate
   */
  public void setPosition(float x, float y) {
    this.position.set(x, y);
  }

  /**
   * Gets the position in 2D space.
   * 
   * @param outputPoint - point into which the position should be written
   * @return the position
   */
  public Vector2 getPosition(Vector2 outputPoint /* output */) {
    if (outputPoint == null) {
      throw new IllegalArgumentException("Null output");
    } else {
      outputPoint.set(position.x, position.y);
    }
    return outputPoint;
  }

  /**
   * Sets the angle. Positive values indicate clockwise rotation.
   * 
   * @param angle - the angle, in radians
   */
  public void setAngle(double angle) {
    this.angle = angle;
  }

  /**
   * @return the angle, in radians.
   */
  public double getAngle() {
    return this.angle;
  }
}
