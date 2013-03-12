package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
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

  public static final float MAX_STRENGTH = 50f;
  private static final float TOLERANCE = 0.1f;
  private float strength = 1;
  
  public BField(float x, float y, float angle) {
    super(ComponentType.BField, x, y, angle);
  }
  
  @Override
  public void initializeConfigs() {
    super.initializeConfigs();
    configs.add(new AbstractModelConfig<Float>(this, 
        Parameter.RotationAngle, 0f, MathUtils.PI * 2) {
      public Float getValue() { return getAngle(); }
      public void setValue(Float value) { setAngle(value); }
      public boolean isPossible() { return isActive(); }
    });
    configs.add(new AbstractModelConfig<Float>(this, 
        Parameter.FieldStrength, 0f, MAX_STRENGTH) {
      public Float getValue() { return getStrength(); }
      public void setValue(Float value) { setStrength(value); }
      public boolean isPossible() { return isActive(); }
    });
  }
  
  /**
   * Sets the magnitude of the BField strength, in Gauss.
   * 
   * @param strength
   *          the strength
   */
  public void setStrength(float strength) {
    if (Math.abs(this.strength - strength) > TOLERANCE) {
      this.strength = strength;
      if (getModel() != null) {
        getModel().notifyFieldChange();
      }
    }
  }

  public void setAngle(float angle) {
    if (Math.abs(getAngle() - angle) > TOLERANCE) {
       setPositionAndAngle(getPosition(), angle);
       if (getModel() != null) {
         getModel().notifyFieldChange();
       }
    }
  }

  /**
   * Gets the magnitude of the field strength, in Gauss.
   * 
   * @return the strength
   */
  public float getStrength() {
    return this.strength;
  }
  
  @Override
  public Vector2 getBField(Vector2 location, Vector2 magneticField) {
    magneticField.set(strength, 0);
    magneticField.setAngle(getAngle() * MathUtils.radiansToDegrees);
    return null;
  }
}
