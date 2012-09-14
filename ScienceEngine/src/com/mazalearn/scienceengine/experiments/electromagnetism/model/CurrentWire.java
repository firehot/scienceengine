// Copyright 2012, Maza Learn Pvt. Ltd.

package com.mazalearn.scienceengine.experiments.electromagnetism.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.EMField.IProducer;

/**
 * Current wire is an infinite wire carrying current
 * 
 * @author sridhar
 */
public class CurrentWire extends ScienceBody implements IProducer {
  // Radius of the wire.
  private float radius;
  // Amplitude of the current in the wire (-1...+1)
  private float current;
  // Direction of current: up is true and down is false
  private boolean direction = true;

  public CurrentWire(EMField emField, float x, float y, float angle) {
    super("CurrentWire", x, y, angle);
    emField.registerProducer(this);
    this.radius = 1;
    this.current = 1f;
    FixtureDef fixtureDef = new FixtureDef();
    CircleShape circleShape = new CircleShape();
    circleShape.setRadius(radius);
    fixtureDef.density = 1;
    fixtureDef.shape = circleShape;
    fixtureDef.filter.categoryBits = 0x0002;
    fixtureDef.filter.maskBits = 0x0000;
    this.createFixture(fixtureDef);
    circleShape.dispose();  // TODO: dispose other created shapes
    initializeConfigs();
  }

  /**
   * Sets the radius of the wire.
   * @param radius - the radius
   */
  public void setRadius(float radius) {
    this.radius = radius;
  }

  /**
   * Gets the radius of the coil.
   * @return the radius
   */
  public float getRadius() {
    return this.radius;
  }

  /**
   * Sets the current in the wire. 
   * @param current the current (-1...+1)
   */
  public void setCurrentMagnitude(float current) {
    this.current = current;
  }
  
  public void flipCurrentDirection() {
    this.direction = !this.direction;
  }
  
  public boolean isDirectionUp() {
    return direction;
  }

  public void initializeConfigs() {
    configs.add(new AbstractModelConfig<Float>("Current", 
        "Current in Wire", 0, 10) {
      public Float getValue() { return getCurrentMagnitude(); }
      public void setValue(Float value) { setCurrentMagnitude(value); }
      public boolean isPossible() { return isActive(); }
    });
    configs.add(new AbstractModelConfig<String>("Flip Current Direction", 
        "Current Direction in Wire") {
      public void doCommand() { flipCurrentDirection(); }
      public boolean isPossible() { return isActive(); }
    });
  }

  /**
   * Gets the current amplitude in the coil.
   * @return the current amplitude
   */
  public float getCurrentMagnitude() {
    return this.current;
  }

  @Override
  public Vector2 getBField(Vector2 location, Vector2 bField) {
    Vector2 localPoint = getLocalPoint(location);
    float magnitude = 10 * current * (direction ? 1 : -1) / localPoint.len();
    localPoint.nor();
    // Current towards me is +
    bField.set(-localPoint.y, localPoint.x).mul(magnitude);
    return bField;
  }
}
