package com.mazalearn.scienceengine.experiments.electromagnetism.model;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mazalearn.scienceengine.box2d.ScienceBody;

/**
 * Models a free north pole which records a trace as it follows the field.
 * It is a point body without width and height.
 * <p/>
 * 
 * @author sridhar
 */
public class FieldSampler extends ScienceBody {

  private static final float TOLERANCE = 0.3f;
  // Field that the free north pole is interacting with.
  private EMField emField;
  // A reusable vector
  private Vector2 fieldVector = new Vector2(), deltaPoint = new Vector2();
  
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
  public FieldSampler(EMField emField, float x, float y, float angle) {
    super("FieldSampler", x, y, angle);
    getBody().setType(BodyType.DynamicBody);
    this.emField = emField;
    FixtureDef fixtureDef = new FixtureDef();
    CircleShape circleShape = new CircleShape();
    circleShape.setRadius(0.1f);
    fixtureDef.density = 1;
    fixtureDef.shape = circleShape;
    fixtureDef.filter.categoryBits = 0x0000;
    fixtureDef.filter.maskBits = 0x0000;
    this.createFixture(fixtureDef);
  }
  
  @Override
  public void singleStep(float dt) {
    float delta = 1;
    if (fieldSamples.size() > 0) {
      FieldSample lastSample = fieldSamples.get(fieldSamples.size() - 1);
      deltaPoint.set(lastSample.x - getPosition().x, lastSample.y - getPosition().y);
      delta = deltaPoint.len();
    }
    if (delta > TOLERANCE) {
      emField.getBField(getPosition(), fieldVector /* output */);
      addFieldSample(getPosition().x, getPosition().y, 
          fieldVector.angle() * MathUtils.degreesToRadians, fieldVector.len());
    }
  }
  
  @Override
  public void resetInitial() {
    super.resetInitial();
    fieldSamples.clear();
  }
  
  public float getBField() {
    return fieldVector.len();
  }

  private void addFieldSample(float x, float y, float angle, float bfield) {
    fieldSamples.add(new FieldSample(x, y, angle, bfield));
  }

  public List<FieldSample> getFieldSamples() {
    return fieldSamples;
  }
}
