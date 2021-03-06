package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mazalearn.scienceengine.core.model.Science2DBody;

/**
 * Compass is the model of a compass - it is a point body without width and height.
 * It shows direction as well as strength of the field at the point
 * <p/>
 * 
 * @author sridhar
 */
public class Compass extends Science2DBody {

  // A reusable vector
  private Vector2 fieldVector = new Vector2();
  private Vector3 bField = new Vector3();
  
  /**
   * @param emField
   */
  public Compass(float x, float y, float angle) {
    super(ComponentType.Compass, x, y, angle);
    FixtureDef fixtureDef = new FixtureDef();
    CircleShape circleShape = new CircleShape();
    circleShape.setRadius(0);
    fixtureDef.density = 1;
    fixtureDef.shape = circleShape;
    fixtureDef.filter.categoryBits = 0x0000;
    fixtureDef.filter.maskBits = 0x0000;
    this.createFixture(fixtureDef);
    circleShape.dispose();
  }
  
  @Override
  public void singleStep(float dt) {
    getModel().getBField(getPosition(), bField /* output */);
    fieldVector.set(bField.x, bField.y);
    float angle = fieldVector.angle() * MathUtils.degreesToRadians;
    setPositionAndAngle(getPosition(), angle);
    super.singleStep(dt);
  }
  
  public float getBField() {
    return fieldVector.len();
  }
}
