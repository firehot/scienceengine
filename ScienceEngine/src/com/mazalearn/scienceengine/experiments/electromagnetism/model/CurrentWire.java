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
  public void setCurrent(float current) {
    this.current = current;
  }

  public void initializeConfigs() {
    configs.add(new AbstractModelConfig<Float>("Current", 
        "Current in Wire", -10, 10) {
      public Float getValue() { return getCurrent(); }
      public void setValue(Float value) { setCurrent(value); }
      public boolean isPossible() { return isActive(); }
    });
  }

  /**
   * Gets the current amplitude in the coil.
   * @return the current amplitude
   */
  public float getCurrent() {
    return this.current;
  }

  @Override
  public Vector2 getBField(Vector2 location, Vector2 bField) {
    Vector2 localPoint = getLocalPoint(location);
    float magnitude = 10 * current / localPoint.len();
    localPoint.nor();
    // Current towards me is +
    bField.set(-localPoint.y, localPoint.x).mul(magnitude);
    return bField;
  }
}
