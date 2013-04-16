package com.mazalearn.scienceengine.domains.electromagnetism.model;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mazalearn.scienceengine.ScreenComponent;
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

  private static final float X_SPACE = 150;
  private static final float Y_SPACE = 150;
  // A reusable vector
  private Vector3 fieldVector = new Vector3();
  private Vector2 samplePoint = new Vector2(), bField = new Vector2();
  public enum SampleMode {User, Uniform};
  private SampleMode sampleMode = SampleMode.User;
  
  // Represent field at a point (x,y) using spherical coords (magnitude, theta, phi)
  public static class FieldSample {
    public float x, y, magnitude, theta, phi;
    /**
     * Constructor
     * @param x
     * @param y
     * @param fieldvector - in rectangular coordinates
     */
    public FieldSample(float x, float y, Vector3 fieldVector) {
      this.x = x;
      this.y = y;
      rectangular2spherical(this, fieldVector);
    }
    
    public static void rectangular2spherical(FieldSample fieldSample, Vector3 fieldVector) {
      if (fieldVector.x != 0 || fieldVector.y != 0) { // Avoid indeterminate theta
        fieldSample.theta = (float) Math.atan(fieldVector.y / fieldVector.x);
      }
      fieldSample.magnitude = fieldVector.len();
      fieldSample.phi = (float) Math.acos(fieldVector.z / fieldSample.magnitude) - MathUtils.PI / 2;
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
    configs.add(new AbstractModelConfig<String>(this, 
        Parameter.SampleMode, SampleMode.values()) {
      public String getValue() { return sampleMode.name(); }
      public void setValue(String value) { setSampleMode(value); }
      public boolean isPossible() { return false; /* only internal use */}
    });
  }

  @Override
  public void setPositionAndAngle(Vector2 position, float angle) {
    super.setPositionAndAngle(position, angle);
    getModel().getBField(getPosition(), fieldVector /* output */);
    addFieldSample(getPosition().x, getPosition().y, fieldVector);
  }
  
  @Override
  public void reset() {
    super.reset();
    fieldSamples.clear();
    //setPositionAndAngle(getPosition(), getAngle());
  }
  
  public float getBField() {
    return fieldVector.len();
  }

  public void addFieldSample(float x, float y, Vector3 fieldVector) {
    bField.set(fieldVector.x, fieldVector.y);
    fieldSamples.add(new FieldSample(x, y, fieldVector));
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
      FieldSample.rectangular2spherical(fieldSample, fieldVector);
    }
  }

  private void setSampleMode(String value) {
    sampleMode = SampleMode.valueOf(value);
    fieldSamples.clear();
    switch (sampleMode) {
    case User:
      break;
    case Uniform:
      // Sample uniformly on X and Y axis and show fields at those points
      for (float x = 0; x < ScreenComponent.VIEWPORT_WIDTH + X_SPACE; x += X_SPACE) {
        for (float y = 0; y < ScreenComponent.VIEWPORT_WIDTH + Y_SPACE; y += Y_SPACE) {
          addFieldSample(x / ScreenComponent.PIXELS_PER_M, y / ScreenComponent.PIXELS_PER_M,
              Vector3.Zero);
        }
      }
      notifyFieldChange();
      break;
    }
  }

  public SampleMode getSampleMode() {
    return sampleMode;
  }
}
