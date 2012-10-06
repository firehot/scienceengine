// Copyright 2012, Maza Learn Pvt. Ltd.

package com.mazalearn.scienceengine.experiments.electromagnetism.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.mazalearn.scienceengine.core.model.ICurrent;
import com.mazalearn.scienceengine.core.model.Science2DBody;

/**
 * Current coil is a coiled rectangular loop carrying current
 * 
 * @author sridhar
 */
public class CurrentCoil extends Science2DBody implements ICurrent.Sink {
  private static final float OUTPUT_SCALE = 0.01f;
  // Dimensions of the coil.
  private float width, height;
  // Current in the wire
  private float current;
  // Field acting on the wire
  private Vector2 forceVector = new Vector2(), pos = new Vector2();

  public CurrentCoil(String name, float x, float y, float angle) {
    super(ComponentType.CurrentCoil, name, x, y, angle);
    this.width = 16f;
    this.height = 2f;
    FixtureDef fixtureDef = new FixtureDef();
    PolygonShape rectangleShape = new PolygonShape();
    rectangleShape.setAsBox(width/2, height/2);
    fixtureDef.density = 1;
    fixtureDef.shape = rectangleShape;
    fixtureDef.filter.categoryBits = 0x0000;
    fixtureDef.filter.maskBits = 0x0000;
    this.createFixture(fixtureDef);
    rectangleShape.dispose();
  }
  
  /**
   * Sets the magnitude of current in the wire. 
   * @param current the current
   */
  public void setCurrent(float current) {
    this.current = current;
  }
  
  /**
   * Gets the current in the wire
   * @return the current
   */
  public float getCurrent() {
    // Effect of split ring commutator - current is reversed from 180-360 rotation
    float angle = getAngle() % (2 * MathUtils.PI);
    return (angle > MathUtils.PI) ? -current : current;
  }

  @Override
  public void singleStep(float dt) {
    // Force is given by B * i * l 
    // magnetic field * current * length
    // Direction is given by Fleming's left hand rule
    getModel().getBField(getPosition(), forceVector /* output */);
    forceVector.mul(getCurrent()).mul(OUTPUT_SCALE).mul(1000);
    forceVector.set(forceVector.y, -forceVector.x);
    applyForce(forceVector, getWorldPoint(pos.set(-width / 2, 0)));
  }

  public float getWidth() {
    return width;
  }

  public float getHeight() {
    return height;
  }
}

