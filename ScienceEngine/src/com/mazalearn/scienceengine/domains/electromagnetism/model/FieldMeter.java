package com.mazalearn.scienceengine.domains.electromagnetism.model;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.model.IMagneticField;
import com.mazalearn.scienceengine.core.model.Science2DBody;

/**
 * Models a field meter which can store multiple samples at 
 * different points in space. 
 * It recalculates the field values when notified of a field change
 * through its consumer interface.
 * It is a point body without width and height.
 * <p/>
 * 
 * @author sridhar
 */
public class FieldMeter extends Science2DBody implements IMagneticField.Consumer {

  // A reusable vector
  private Vector3 fieldVector = new Vector3();
  private Vector2 samplePoint = new Vector2(), bField = new Vector2();
  
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
  public FieldMeter(float x, float y, float angle) {
    super(ComponentType.FieldMeter, x, y, angle);
    getBody().setType(BodyType.DynamicBody);
    FixtureDef fixtureDef = new FixtureDef();
    CircleShape circleShape = new CircleShape();
    circleShape.setRadius(0.1f);
    fixtureDef.density = 1;
    fixtureDef.shape = circleShape;
    fixtureDef.filter.categoryBits = 0x0000;
    fixtureDef.filter.maskBits = 0x0000;
    this.createFixture(fixtureDef);
    circleShape.dispose();
  }
  
  public void initializeConfigs() {
    configs.add(new AbstractModelConfig<Float>(this, 
        Parameter.Count, 0f, 1000f) {
      public Float getValue() { return (float) fieldSamples.size(); }
      public void setValue(Float value) { /* Ignore */ }
      public boolean isMeter() { return true; }
      public boolean isPossible() { return isActive(); }
    });
  }

  @Override
  public void setPositionAndAngle(Vector2 position, float angle) {
    super.setPositionAndAngle(position, angle);
    getModel().getBField(getPosition(), fieldVector /* output */);
    bField.set(fieldVector.x, fieldVector.y);
    addFieldSample(getPosition().x, getPosition().y, 
        bField.angle() * MathUtils.degreesToRadians, bField.len());
  }
  
  @Override
  public void reset() {
    super.reset();
    fieldSamples.clear();
  }
  
  public float getBField() {
    return fieldVector.len();
  }

  public void addFieldSample(float x, float y, float angle, float bfield) {
    fieldSamples.add(new FieldSample(x, y, angle, bfield));
  }

  public List<FieldSample> getFieldSamples() {
    return fieldSamples;
  }

  @Override
  public void setBField(Vector3 bField) {
    fieldVector.set(bField);
  }
  
  @Override
  public boolean allowsConfiguration() {
    return false;
  }

  @Override
  public void notifyFieldChange() {
    for (FieldSample fieldSample: fieldSamples) {
      samplePoint.set(fieldSample.x, fieldSample.y);
      getModel().getBField(samplePoint, fieldVector /* output */);
      bField.set(fieldVector.x, fieldVector.y);
      fieldSample.angle = bField.angle() * MathUtils.degreesToRadians;
      fieldSample.magnitude = bField.len();
    }
  }
}
