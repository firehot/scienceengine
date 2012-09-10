package com.mazalearn.scienceengine.experiments.electromagnetism.model;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.mazalearn.scienceengine.box2d.ScienceBody;

/**
 * Compass is the model of a compass - it is a point body without width and height.
 * It shows direction as well as strength of the field at the point
 * <p/>
 * 
 * @author sridhar
 */
public class Compass extends ScienceBody {

  // Field that the compass is observing.
  private EMField emField;
  // A reusable vector
  private Vector2 fieldVector = new Vector2();
  
  public static class FieldSample {
    public float x, y, angle, magnitude;
    public FieldSample(float x, float y, float angle, float magnitude) {
      this.x = x;
      this.y = y;
      this.angle = angle;
      this.magnitude = magnitude;
    }
  }
  
  private List<FieldSample> fieldSamples = new ArrayList<FieldSample>();

  /**
   * @param emField
   */
  public Compass(EMField emField, float x, float y, float angle) {
    super("Compass", x, y, angle);
    getBody().setType(BodyType.StaticBody);
    this.emField = emField;
    FixtureDef fixtureDef = new FixtureDef();
    CircleShape circleShape = new CircleShape();
    circleShape.setRadius(0);
    fixtureDef.density = 1;
    fixtureDef.shape = circleShape;
    fixtureDef.filter.categoryBits = 0x0000;
    fixtureDef.filter.maskBits = 0x0000;
    this.createFixture(fixtureDef);
  }
  
  @Override
  public void singleStep(float dt) {
    emField.getBField(getPosition(), fieldVector /* output */);
    float angle = fieldVector.angle() * MathUtils.degreesToRadians;
    setPositionAndAngle(getPosition(), angle);
  }
  
  @Override
  public void resetInitial() {
    super.resetInitial();
    fieldSamples.clear();
  }
  
  public float getBField() {
    return fieldVector.len();
  }

  public void addFieldSample(float x, float y) {
    fieldSamples.add(new FieldSample(x, y, getAngle(), getBField()));
  }

  public List<FieldSample> getFieldSamples() {
    return fieldSamples;
  }
}
