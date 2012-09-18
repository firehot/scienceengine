// Copyright 2002-2012, University of Colorado

package com.mazalearn.scienceengine.experiments.electromagnetism.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.controller.AbstractModelConfig;

/**
 * AbstractMagnet is the abstract base class for all magnets.
 * 
 * @author sridhar
 */
public abstract class AbstractMagnet extends ScienceBody 
       implements EMField.IProducer {

  private float width, height;
  private float strength;

  /**
   * Sole constructor
   * @param  emField - Electromagnetic field to which magnet is coupled
   */
  public AbstractMagnet(ComponentType componentType, String name, EMField emField, float x, float y, float angle) {
    super(componentType, name, x, y, angle);
    emField.registerProducer(this);
    
    this.width = 32;
    this.height = 8;
    FixtureDef fixtureDef = new FixtureDef();
    PolygonShape rectangleShape = new PolygonShape();
    rectangleShape.setAsBox(this.width/2, this.height/2);
    fixtureDef.density = 1;
    fixtureDef.shape = rectangleShape;
    fixtureDef.filter.categoryBits = 0x0001;
    fixtureDef.filter.maskBits = 0x0001;
    this.createFixture(fixtureDef);

    this.strength = 1.0f;
    initializeConfigs();
  }
  
  public void initializeConfigs() {
    configs.add(new AbstractModelConfig<Float>(getName() + "Strength", 
        "Strength of magnet", 0f, 10000f) {
      public Float getValue() { return getStrength(); }
      public void setValue(Float value) { setStrength(value); }
      public boolean isPossible() { return isActive(); }
    });
  }

  /**
   * Flips the magnet's polarity by rotating it 180 degrees.
   */
  public void flipPolarity() {
    setPositionAndAngle(getPosition().x, getPosition().y, 
        (float) ((getAngle() + Math.PI) % (2 * Math.PI)));
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
    this.strength = strength;
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
    localPoint.sub(width/2, height/2);
    localPoint.mul(5.0f); // fudge factor to keep at 250,50 scale
    // get strength in magnet's local coordinate frame
    getBFieldRelative(localPoint, outputVector);

    // Adjust the field vector to match the magnet's angle.
    outputVector.rotate(getAngle() * MathUtils.radiansToDegrees);

    // Clamp magnitude to magnet strength.
    // TODO: why do we need to do this?
    float magnetStrength = getStrength();
    float magnitude = outputVector.len();
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
