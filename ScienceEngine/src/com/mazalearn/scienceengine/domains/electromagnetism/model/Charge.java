package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.model.IMagneticField;
import com.mazalearn.scienceengine.core.model.Science2DBody;

/**
 * Monopole is the model of a virtual magnetic pole.
 * It reacts to direction as well as charge of the field at the point
 * <p/>
 * 
 * @author sridhar
 */
public class Charge extends Science2DBody implements IMagneticField.Consumer {

  private static final float MAX_STRENGTH = 10;
  // Magnetic Field acting on pole
  private Vector3 fieldVector = new Vector3(), velocity = new Vector3();
  private Vector2 force = new Vector2();
  // charge
  private float charge = 1;
  
  public Charge(float x, float y, float angle) {
    super(ComponentType.Charge, x, y, angle);
    getBody().setType(BodyType.DynamicBody);
    FixtureDef fixtureDef = new FixtureDef();
    CircleShape circleShape = new CircleShape();
    circleShape.setRadius(10);
    fixtureDef.density = 0.2f;
    fixtureDef.shape = circleShape;
    fixtureDef.filter.categoryBits = 0x0000;
    fixtureDef.filter.maskBits = 0x0000;
    this.createFixture(fixtureDef);
    circleShape.dispose();
  }
  
  @Override
  public void initializeConfigs() {
    super.initializeConfigs();
    configs.add(new AbstractModelConfig<Float>(this, 
        Parameter.ChargeStrength, -MAX_STRENGTH, MAX_STRENGTH) {
      public Float getValue() { return getStrength(); }
      public void setValue(Float value) { setStrength(value); }
      public boolean isPossible() { return isActive(); }
    });
  }
  
  public float getStrength() {
    return charge;
  }
  
  public void setStrength(float strength) {
    this.charge = strength;
    System.out.println(strength);
  }
  
  @Override
  public void reset() {
    super.reset();
    fieldVector.set(0, 0, 0);
  }
  @Override
  public void singleStep(float dt) {
    super.singleStep(dt);
    // -q(V x B)
    velocity.set(getLinearVelocity().x, getLinearVelocity().y, 0).crs(fieldVector);
    force.set(velocity.x, velocity.y).mul(-charge);
    applyForce(force, getWorldCenter());
  }

  @Override
  public void setBField(Vector3 magneticField) {
    fieldVector.set(magneticField);
    
  }

  @Override
  public void notifyFieldChange() {
    getModel().getBField(getPosition(), fieldVector);
  }
}
