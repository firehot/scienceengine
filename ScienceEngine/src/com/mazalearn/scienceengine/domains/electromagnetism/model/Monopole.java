package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.model.Science2DBody;

/**
 * Monopole is the model of a virtual magnetic pole.
 * It reacts to direction as well as strength of the field at the point
 * <p/>
 * 
 * @author sridhar
 */
public class Monopole extends Science2DBody {

  // Magnetic Field acting on pole
  private Vector2 fieldVector = new Vector2(), force = new Vector2();
  // type of commutator connector
  public enum MonopoleType { NorthPole, SouthPole};
  
  private MonopoleType monopoleType = MonopoleType.NorthPole;
  
  public Monopole(float x, float y, float angle) {
    super(ComponentType.Monopole, x, y, angle);
    getBody().setType(BodyType.DynamicBody);
    FixtureDef fixtureDef = new FixtureDef();
    CircleShape circleShape = new CircleShape();
    circleShape.setRadius(10);
    fixtureDef.density = 1;
    fixtureDef.shape = circleShape;
    fixtureDef.filter.categoryBits = 0x0000;
    fixtureDef.filter.maskBits = 0x0000;
    this.createFixture(fixtureDef);
    circleShape.dispose();
  }
  
  @Override
  public void initializeConfigs() {
    super.initializeConfigs();
    configs.add(new AbstractModelConfig<String>(this, 
        Parameter.MonopoleType, MonopoleType.values()) {
      public String getValue() { return monopoleType.name(); }
      public void setValue(String value) { monopoleType = MonopoleType.valueOf(value); }
      public boolean isPossible() { return isActive(); }
    });
  }
  
  public MonopoleType getPoleType() {
    return monopoleType;
  }
  
  @Override
  public void reset() {
    super.reset();
    fieldVector.set(0,0);
  }
  @Override
  public void singleStep(float dt) {
    super.singleStep(dt);
    if (monopoleType == MonopoleType.NorthPole) {
      force.set(fieldVector.x, fieldVector.y);
    } else { // South Monopole
      force.set(-fieldVector.x, -fieldVector.y);
    }
    applyForce(force, getWorldCenter());
  }

  public void setField(Vector2 field) {
    fieldVector.set(field);
  }
}
