package com.mazalearn.scienceengine.domains.mechanics.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mazalearn.scienceengine.core.model.Science2DBody;

/**
 * SimpleBody is the model of a virtual magnetic pole.
 * It reacts to direction as well as strength of the field at the point
 * <p/>
 * 
 * @author sridhar
 */
public class SimpleBody extends Science2DBody {

  // Force acting on body
  private Vector2 force = new Vector2();
  
  public SimpleBody(float x, float y, float angle) {
    super(ComponentType.SimpleBody, x, y, angle);
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
  public void reset() {
    super.reset();
    force.set(0,0);
  }
  
  @Override
  public void singleStep(float dt) {
    super.singleStep(dt);
    applyForce(force, getWorldCenter());
  }

  public void setForce(Vector2 field) {
    force.set(field);
  }
}
