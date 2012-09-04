package com.mazalearn.scienceengine.experiments.electromagnetism.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.mazalearn.scienceengine.box2d.ScienceBody;

/**
 * Compass is the model of a compass.
 * However, it shows direction as well as strength of the field.
 * <p/>
 * 
 * @author sridhar
 */
public class Compass extends ScienceBody {

  public final float width = 01f;
  public final float height = 10f;
  // Field that the compass is observing.
  private EMField emField;
  // A reusable vector
  private Vector2 fieldVector;
  private RevoluteJointDef jointDef = new RevoluteJointDef();
  private Joint joint;

  /**
   * @param emField
   */
  public Compass(EMField emField, float x, float y, float angle) {
    super("Compass", x, y, angle);
    getBody().setType(BodyType.StaticBody);
    this.emField = emField;
    PolygonShape rectangleShape = new PolygonShape();
    rectangleShape.setAsBox(width, height);
    this.createFixture(rectangleShape, 0.01f);
    this.setAngularDamping(0.1f);

    fieldVector = new Vector2();
  }
  
  @Override
  public void setPositionAndAngle(Vector2 position, float angle) {
    emField.getBField(position, fieldVector /* output */);
    super.setPositionAndAngle(position, fieldVector.angle());  
  }
   
  public void singleStep(float dt) {
    setPositionAndAngle(getPosition(), getAngle());
  }

}
