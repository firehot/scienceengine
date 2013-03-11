package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.mazalearn.scienceengine.core.model.Science2DBody;

/**
 * Pole is the model of a virtual magnetic pole.
 * It reacts to direction as well as strength of the field at the point
 * <p/>
 * 
 * @author sridhar
 */
public class Pole extends Science2DBody {

  // A reusable vector
  private Vector2 fieldVector = new Vector2();
  
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
  public void singleStep(float dt) {
    getModel().getBField(getPosition(), fieldVector /* output */);
    float angle = fieldVector.angle() * MathUtils.degreesToRadians;
    setPositionAndAngle(getPosition(), angle);
    super.singleStep(dt);
  }
}
