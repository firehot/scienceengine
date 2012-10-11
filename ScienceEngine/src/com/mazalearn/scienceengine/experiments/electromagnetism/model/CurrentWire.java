// Copyright 2012, Maza Learn Pvt. Ltd.

package com.mazalearn.scienceengine.experiments.electromagnetism.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mazalearn.scienceengine.core.model.ICurrent;
import com.mazalearn.scienceengine.core.model.IMagneticField;
import com.mazalearn.scienceengine.core.model.Science2DBody;

/**
 * Current wire is an infinite wire carrying current
 * 
 * @author sridhar
 */
public class CurrentWire extends Science2DBody implements IMagneticField.Producer, ICurrent.Sink {
  private static final int OUTPUT_SCALE = 50;
  // Radius of the wire.
  private float radius;
  // Current in the wire
  private float current;
  // Field acting on the wire
  private Vector2 forceVector = new Vector2();

  public CurrentWire(String name, float x, float y, float angle) {
    super(ComponentType.CurrentWire, name, x, y, angle);
    this.radius = 0.1f;
    this.current = 1f;
    FixtureDef fixtureDef = new FixtureDef();
    CircleShape circleShape = new CircleShape();
    circleShape.setRadius(radius);
    fixtureDef.density = 1;
    fixtureDef.shape = circleShape;
    fixtureDef.filter.categoryBits = 0x0002;
    fixtureDef.filter.maskBits = 0x0000;
    this.createFixture(fixtureDef);
    circleShape.dispose();
  }
  
  /**
   * Sets the magnitude of current in the wire. 
   * @param current the current
   */
  public void setCurrent(float current) {
    if (this.current != current) {
      this.current = current;
      getModel().notifyFieldChange();
    }
  }
  
  /**
   * Gets the current in the wire
   * @return the current
   */
  public float getCurrent() {
    return this.current;
  }

  @Override
  public Vector2 getBField(Vector2 location, Vector2 bField) {
    Vector2 localPoint = getLocalPoint(location);
    // field = constant * current / distance
    float magnitude = OUTPUT_SCALE * current / localPoint.len();
    localPoint.nor();
    // Current towards me is +
    bField.set(-localPoint.y, localPoint.x).mul(magnitude);
    return bField;
  }
  
  @Override
  public void singleStep(float dt) {
    // Force is given by B * i * l 
    // magnetic field * current * length
    // Direction is given by Fleming's left hand rule
    getModel().getBField(getPosition(), forceVector /* output */);
    forceVector.mul(getCurrent()).mul(0.01f); // TODO: tune multiplier
    applyForce(forceVector, getWorldCenter());
  }
  
  public Vector2 getBForce() {
    return forceVector;
  }
  
  @Override
  public Vector2 getFirstTerminalPosition() {
    return new Vector2(-10, -10);
  }

  @Override
  public Vector2 getSecondTerminalPosition() {
    return new Vector2(-10, -10);
  }
}
