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
public class MagneticField extends Science2DBody implements IMagneticField.Producer {

  public enum Phi { XYPlane, Up, Down };

  public static final float MAX_STRENGTH = 30f;
  private final Vector3 field3 = new Vector3(0.5f, 0.5f, 0);
  private final Vector2 field2 = new Vector2();
  private Phi phi = Phi.XYPlane; 
  
  public MagneticField(float x, float y, float angle) {
    super(ComponentType.MagneticField, x, y, angle);
  }
  
  @Override
  public void initializeConfigs() {
    super.initializeConfigs();
    configs.add(new AbstractModelConfig<Float>(this, 
        Parameter.FieldStrength, 0.5f, MAX_STRENGTH) {
      public Float getValue() { return field3.len(); }
      public void setValue(Float value) { field3.nor().mul(value); getModel().notifyFieldChange(); }
      public boolean isPossible() { return isActive(); }
    });
    configs.add(new AbstractModelConfig<String>(this, 
        Parameter.FieldUpDown, Phi.values()) {
      public String getValue() { return getPhi(); }
      public void setValue(String value) { setPhi(value); }
      public boolean isPossible() { return isActive(); }
    });
    configs.add(new AbstractModelConfig<Float>(this, 
        Parameter.FieldAngle, 0, 2 * MathUtils.PI) {
      public Float getValue() { return getTheta(); }
      public void setValue(Float angle) { setTheta(angle); }
      public boolean isPossible() { return isActive() && phi == Phi.XYPlane; }
    });    
  }
  
  public float getStrength() {
    return field3.len();
  }
  
  public float getAngle() {
    return getTheta();
  }
  
  @Override
  public Vector3 getBField(Vector2 location, Vector3 magneticField) {
    return magneticField.set(field3);
  }

  public void setTheta(Float angle) {
    field2.set(field3.x, field3.y);
    if (angle == field2.angle()) return;
    float len = field3.len();
    field3.y = len * MathUtils.sin(angle);
    field3.x = len * MathUtils.cos(angle);
    field3.z = 0;
    getModel().notifyFieldChange();
  }

  public float getTheta() {
    if (field3.z != 0) return 0;
    field2.set(field3.x, field3.y);
    return field2.angle() * MathUtils.degreesToRadians;
  }

  public void setPhi(String value) {
    Phi ph = Phi.valueOf(value);
    if (ph != phi) {
      phi = ph;
      // Preserve magnitude and assign direction
      float magnitude = field3.len();
      switch (phi) {
      case XYPlane: 
        field3.set((float) Math.sqrt(magnitude), (float) Math.sqrt(magnitude), 0);
        break;
      case Up:
        field3.set(0, 0, magnitude);
        break;
      case Down:
        field3.set(0, 0, -magnitude);
        break;
      }
      getModel().notifyFieldChange();
    }
  }

  public String getPhi() {
    return phi.name();
  }
}
