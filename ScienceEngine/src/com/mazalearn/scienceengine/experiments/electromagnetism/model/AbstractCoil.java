// Copyright 2002-2011, University of Colorado

package com.mazalearn.scienceengine.experiments.electromagnetism.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.mazalearn.scienceengine.core.model.Science2DBody;

/**
 * AbstractCoil is the abstract base class for all coils.
 * TODO: does not seem to be required.
 * 
 * @author sridhar
 */
public abstract class AbstractCoil extends Science2DBody {

  // Number of loops in the coil.
  private int numberOfLoops;
  // Radius of all loops in the coil.
  private float radius;
  // Width of the wire.
  private float wireWidth;
  // Spacing between the loops
  private float loopSpacing;
  // Amplitude of the current in the coil (-1...+1)
  private float current;

  // ----------------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------------

  /**
   * Zero-argument constructor. Creates a default coil with one loop, radius=10,
   * wireWidth=16, loopSpacing=25
   */
  public AbstractCoil(ComponentType componentType, String name, float x, float y, float angle) {
    this(componentType, name, x, y, angle, 1, 8, 16, 25);
  }

  /**
   * Fully-specified constructor.
   * 
   * @param numberOfLoops -  number of loops in the coil
   * @param radius - radius used for all loops
   * @param wireWidth - width of the wire
   * @param loopSpacing - space between the loops
   */
  public AbstractCoil(ComponentType componentType, String name, float x, float y, float angle, 
      int numberOfLoops, float radius, float wireWidth, float loopSpacing) {
    super(componentType, name, x, y, angle);
    this.numberOfLoops = numberOfLoops;
    this.radius = radius;
    this.wireWidth = wireWidth;
    this.loopSpacing = loopSpacing;
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

  /**
   * Sets the radius of the coil. This radius is shared by all loops in the
   * coil.
   * 
   * @param radius
   *          the radius
   */
  public void setRadius(float radius) {
    assert (radius > 0);
    this.radius = radius;
  }

  /**
   * Gets the radius of the coil.
   * 
   * @return the radius
   */
  public float getRadius() {
    return this.radius;
  }

  /**
   * Sets the surface area of one loop.
   * 
   * @param area
   *          the area
   */
  public void setLoopArea(float area) {
    float radius = (float) Math.sqrt(area / Math.PI);
    setRadius(radius);
  }

  /**
   * Gets the surface area of one loop.
   * 
   * @return the area
   */
  public float getLoopArea() {
    return (MathUtils.PI * this.radius * this.radius);
  }

  /**
   * Sets the width of the wire used for the coil.
   * 
   * @param wireWidth - the wire width, in pixels
   */
  public void setWireWidth(float wireWidth) {
    assert (wireWidth > 0);
    this.wireWidth = wireWidth;
  }

  /**
   * Gets the width of the wire used for the coil.
   * 
   * @return the wire width, in pixels
   */
  public float getWireWidth() {
    return this.wireWidth;
  }

  /**
   * Sets the spacing between loops in the coil.
   * 
   * @param loopSpacing - the spacing, in pixels
   */
  public void setLoopSpacing(float loopSpacing) {
    assert (loopSpacing > 0);
    this.loopSpacing = loopSpacing;
  }

  /**
   * Gets the spacing between loops in the coil.
   * 
   * @return the spacing, in pixels
   */
  public float getLoopSpacing() {
    return this.loopSpacing;
  }

  /*
   * Sets the current in the coil. This should only be called by the
   * coil itself. 
   * 
   * @param current - the current in the coil
   */
  protected void setCurrent(float current) {
    this.current = current;
  }

  /**
   * Gets the current in the coil, in amperes
   */
  public float getCurrent() {
    return this.current;
  }
}
