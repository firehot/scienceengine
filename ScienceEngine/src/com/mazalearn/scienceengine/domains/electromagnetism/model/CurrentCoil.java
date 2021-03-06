// Copyright 2012, Maza Learn Pvt. Ltd.

package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.model.ICurrent;
import com.mazalearn.scienceengine.core.model.Science2DBody;

/**
 * Current coil is a coiled rectangular loop carrying current
 * 
 * @author sridhar
 */
public class CurrentCoil extends Science2DBody implements ICurrent.Sink {
  private static final float OUTPUT_SCALE = 10f;
  // Dimensions of the coil.
  private float width, height;
  // Current in the wire
  private float current;
  // type of commutator connector
  public enum CommutatorType { Connector, Commutator, Disconnected};
  
  private CommutatorType commutatorType = CommutatorType.Disconnected;
  // Field acting on the wire
  private Vector2 forceVector = new Vector2(), pos = new Vector2();
  private Vector3 bField = new Vector3();
  // Terminals
  private Vector2 firstTerminal = new Vector2(), secondTerminal = new Vector2();
  private enum RotationDataType {
    AngularVelocity,
    NumRevolutions;
  }
  private RotationDataType rotationDataType = RotationDataType.AngularVelocity;

  public CurrentCoil(float x, float y, float angle) {
    super(ComponentType.CurrentCoil, x, y, angle);
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
    this.setAngularDamping(2f);
    rectangleShape.dispose();
  }

  @Override
  public void initializeConfigs() {
    super.initializeConfigs();
    configs.add(new AbstractModelConfig<String>(this, 
        Parameter.CommutatorType, CommutatorType.values()) {
      public String getValue() { return commutatorType.name(); }
      public void setValue(String value) { commutatorType = CommutatorType.valueOf(value); }
      public boolean isPossible() { return isActive(); }
    });
    configs.add(new AbstractModelConfig<String>(this, 
        Parameter.RotationDataType, RotationDataType.values()) {
      public String getValue() { return rotationDataType.name(); }
      public void setValue(String value) { 
        rotationDataType = RotationDataType.valueOf(value);
        angleCovered = 0;
      }
      public boolean isPossible() { return isActive() && MovementMode.Rotate.name().equals(getMovementMode()); }
    });
    configs.add(new AbstractModelConfig<Float>(this, 
        Parameter.RotationData, -1000, 1000) { // Still risky, limits may get exceeded
      public Float getValue() { return getRotationData(); }
      public void setValue(Float value) {}
      public boolean isMeter() { return true; }
      public boolean isPossible() { return isActive(); }
    });
  }
  
  public Float getRotationData() {
    return rotationDataType == RotationDataType.AngularVelocity 
        ? getAngularVelocity() : getNumRevolutions();
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
    switch (commutatorType) {
    case Commutator:
      // Effect of split ring commutator
      // current is reversed from 90-270 i.e. pi/2 - 3pi/2 rotation
      float angle = getAngle() % (2 * MathUtils.PI);
      return (angle > MathUtils.PI * 0.5 && angle <= MathUtils.PI * 1.5) ? -current : current;
    case Connector:
      return current;
    default:
      return 0;
    }
  }

  @Override
  public void singleStep(float dt) {
    // Force is given by B * i * l 
    // magnetic field * current * length
    // Direction is given by Fleming's left hand rule
    getModel().getBField(getPosition(), bField /* output */);
    forceVector.set(bField.x, bField.y).mul(getCurrent()).mul(OUTPUT_SCALE);
    forceVector.set(forceVector.y, -forceVector.x);
    applyForce(forceVector, getWorldPoint(pos.set(-width / 2, 0)));
    super.singleStep(dt);
  }

  public float getWidth() {
    return width;
  }

  public float getHeight() {
    return height;
  }

  public CommutatorType getCommutatorType() {
    return commutatorType;
  }

  @Override
  public Vector2 getT1Position() {
    return firstTerminal.set(getPosition())
        .add(ScreenComponent.getScaledX(2.5f), ScreenComponent.getScaledY(0));
  }

  @Override
  public Vector2 getT2Position() {
    return secondTerminal.set(getPosition())
        .add(ScreenComponent.getScaledX(-2.5f), ScreenComponent.getScaledY(0));
  }
}

