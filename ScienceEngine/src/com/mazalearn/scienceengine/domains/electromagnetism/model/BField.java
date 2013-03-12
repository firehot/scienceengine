package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.model.IMagneticField;
import com.mazalearn.scienceengine.core.model.Science2DBody;

/**
 * Models a homogeneous uniform field with certain strength and rotation. 
 * <p/>
 * 
 * @author sridhar
 */
public class BField extends Science2DBody implements IMagneticField.Producer {

  public static final float MAX_STRENGTH = 30f;
  private final Vector3 field3 = new Vector3();
  private final Vector2 field2 = new Vector2();
  
  public BField(float x, float y, float angle) {
    super(ComponentType.BField, x, y, angle);
  }
  
  @Override
  public void initializeConfigs() {
    super.initializeConfigs();
    configs.add(new AbstractModelConfig<Float>(this, 
        Parameter.FieldStrengthX, -MAX_STRENGTH, MAX_STRENGTH) {
      public Float getValue() { return field3.x; }
      public void setValue(Float value) { field3.x = value;  getModel().notifyFieldChange(); }
      public boolean isPossible() { return isActive() && field3.z == 0; }
    });
    configs.add(new AbstractModelConfig<Float>(this, 
        Parameter.FieldStrengthY, -MAX_STRENGTH, MAX_STRENGTH) {
      public Float getValue() { return field3.y; }
      public void setValue(Float value) { field3.y = value;  getModel().notifyFieldChange(); }
      public boolean isPossible() { return isActive() && field3.z == 0; }
    });
    configs.add(new AbstractModelConfig<Float>(this, 
        Parameter.FieldStrengthZ, -MAX_STRENGTH, MAX_STRENGTH) {
      public Float getValue() { return field3.z; }
      public void setValue(Float value) { field3.z = value; getModel().notifyFieldChange(); }
      public boolean isPossible() { return isActive() && field3.y == 0 && field3.x == 0; }
    });
  }
  
  public float getStrength() {
    if (field3.z != 0) return Math.abs(field3.z);
    field2.set(field3.x, field3.y);
    return field2.len();
  }
  
  public float getAngle() {
    if (field3.z != 0) return 0;
    field2.set(field3.x, field3.y);
    return field2.angle() * MathUtils.degreesToRadians;
  }
  
  @Override
  public Vector3 getBField(Vector2 location, Vector3 magneticField) {
    return magneticField.set(field3);
  }

  public float getBFieldZ() {
    return field3.z;
  }
}
