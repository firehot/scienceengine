package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.domains.electromagnetism.model.CurrentCoil.CommutatorType;

/**
 * Pole is the model of a virtual magnetic pole.
 * It reacts to direction as well as strength of the field at the point
 * <p/>
 * 
 * @author sridhar
 */
public class Pole extends Science2DBody {

  // Magnetic Field acting on pole
  private Vector2 fieldVector = new Vector2(), force = new Vector2();
  // type of commutator connector
  public enum PoleType { NorthPole, SouthPole};
  
  private PoleType poleType = PoleType.NorthPole;
  
  public Pole(float x, float y, float angle) {
    super(ComponentType.Pole, x, y, angle);
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
        Parameter.PoleType, PoleType.values()) {
      public String getValue() { return poleType.name(); }
      public void setValue(String value) { poleType = PoleType.valueOf(value); }
      public boolean isPossible() { return isActive(); }
    });
  }
  
  public PoleType getPoleType() {
    return poleType;
  }
  
  @Override
  public void reset() {
    super.reset();
    fieldVector.set(0,0);
  }
  @Override
  public void singleStep(float dt) {
    super.singleStep(dt);
    if (poleType == PoleType.NorthPole) {
      force.set(fieldVector.x, fieldVector.y);
    } else { // South Pole
      force.set(-fieldVector.x, -fieldVector.y);
    }
    applyForce(force, getWorldCenter());
  }

  public void setField(Vector2 field) {
    fieldVector.set(field);
  }
}
