// Copyright 2002-2011, University of Colorado

package com.mazalearn.scienceengine.experiments.electromagnetism.model;

import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mazalearn.scienceengine.box2d.ScienceBody;

/**
 * AbstractCoil is the abstract base class for all coils.
 * 
 * @author sridhar
 */
public abstract class AbstractCoil extends ScienceBody {

  // ----------------------------------------------------------------------------
  // Instance data
  // ----------------------------------------------------------------------------

  // Number of loops in the coil.
  private int numberOfLoops;
  // Radius of all loops in the coil.
  private float radius;
  // Width of the wire.
  private float wireWidth;
  // Spacing between the loops
  private float loopSpacing;
  // Amplitude of the current in the coil (-1...+1)
  private float currentAmplitude;

  // ----------------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------------

  /**
   * Zero-argument constructor. Creates a default coil with one loop, radius=10,
   * wireWidth=16, loopSpacing=25
   */
  public AbstractCoil(String name, float x, float y, float angle) {
    this(name, x, y, angle, 1, 20, 16, 25);
  }

  /**
   * Fully-specified constructor.
   * 
   * @param numberOfLoops -  number of loops in the coil
   * @param radius - radius used for all loops
   * @param wireWidth - width of the wire
   * @param loopSpacing - space between the loops
   */
  public AbstractCoil(String name, float x, float y, float angle, 
      int numberOfLoops, float radius, float wireWidth, float loopSpacing) {
    super(name, x, y, angle);
    this.numberOfLoops = numberOfLoops;
    this.radius = radius;
    this.wireWidth = wireWidth;
    this.loopSpacing = loopSpacing;
    this.currentAmplitude = 0f;
    FixtureDef fixtureDef = new FixtureDef();
    CircleShape circleShape = new CircleShape();
    circleShape.setRadius(radius);
    fixtureDef.density = 1;
    fixtureDef.shape = circleShape;
    fixtureDef.filter.categoryBits = 0x0002;
    fixtureDef.filter.maskBits = 0x0000;
    this.createFixture(fixtureDef);
  }

  // ----------------------------------------------------------------------------
  // Accessors
  // ----------------------------------------------------------------------------

  /**
   * Sets the number of loops in the coil. This method destroys any existing
   * loops and creates a new set.
   * 
   * @param numberOfLoops
   *          the number of loops
   */
  public void setNumberOfLoops(int numberOfLoops) {
    assert (numberOfLoops > 0);
    this.numberOfLoops = numberOfLoops;
  }

  /**
   * Gets the number of loops in the coil.
   * 
   * @return the number of loops
   */
  public int getNumberOfLoops() {
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
  public double getLoopArea() {
    return (Math.PI * this.radius * this.radius);
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
   * Sets the current amplitude in the coil. This should only be called by the
   * coil itself. <p> This is a quantity that we made up. It is a percentage
   * that describes the amount of current relative to some maximum current in
   * the model, and angle of that current. View components can use this value to
   * determine how they should behave (eg, how far to move a voltmeter needle,
   * how bright to make a lightbulb, etc.)
   * 
   * @param currentAmplitude the current amplitude (-1...+1)
   */
  protected void setCurrentAmplitude(float currentAmplitude) {
    if (currentAmplitude < -1 || currentAmplitude > 1) {
      throw new IllegalArgumentException("currentAmplitude is out of range: "
          + currentAmplitude);
    }
    this.currentAmplitude = currentAmplitude;
  }

  /**
   * Gets the current amplitude in the coil.
   * 
   * @return the current amplitude
   */
  public float getCurrentAmplitude() {
    return this.currentAmplitude;
  }
}
