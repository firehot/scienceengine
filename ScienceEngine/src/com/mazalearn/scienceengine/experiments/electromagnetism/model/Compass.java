package com.mazalearn.scienceengine.experiments.electromagnetism.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.mazalearn.scienceengine.box2d.ScienceBody;

/**
 * Compass is the model of a compass.
 * However, it shows direction as well as strength of the field.
 * <p/>
 * 
 * @author sridhar
 */
public class Compass extends ScienceBody {

  public final float width = 8f;
  public final float height = 2f;
  // Field that the compass is observing.
  private EMField emField;
  // A reusable vector
  private Vector2 fieldVector = new Vector2();
  private Vector2 pos = new Vector2();

  /**
   * @param emField
   */
  public Compass(EMField emField, float x, float y, float angle) {
    super("Compass", x, y, angle);
    getBody().setType(BodyType.StaticBody);
    this.emField = emField;
    FixtureDef fixtureDef = new FixtureDef();
    PolygonShape rectangleShape = new PolygonShape();
    rectangleShape.setAsBox(width/2, height/2);
    fixtureDef.density = 1;
    fixtureDef.shape = rectangleShape;
    fixtureDef.filter.categoryBits = 0x0000;
    fixtureDef.filter.maskBits = 0x0000;
    this.createFixture(fixtureDef);
  }
  
  @Override
  public void singleStep(float dt) {
    pos.set(width/2, height/2);
    pos = getWorldPoint(pos);
    emField.getBField(pos, fieldVector /* output */);
    float angle = fieldVector.angle() * MathUtils.degreesToRadians;
    setPositionAndAngle(getPosition(), angle);
  }
  
  public float getBField() {
    return fieldVector.len();
  }
}
