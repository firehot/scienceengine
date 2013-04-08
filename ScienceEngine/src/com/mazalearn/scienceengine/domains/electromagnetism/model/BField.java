package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.model.IMagneticField;
import com.mazalearn.scienceengine.core.model.Science2DBody;

enum Phi { Up, Down };
/**
 * Models a homogeneous uniform field with certain strength and rotation. 
 * <p/>
 * 
 * @author sridhar
 */
public class BField extends Science2DBody implements IMagneticField.Producer {

  public static final float MAX_STRENGTH = 30f;
  private final Vector3 field3 = new Vector3(0.5f, 0.5f, 0);
  private final Vector2 field2 = new Vector2();
  
  public BField(float x, float y, float angle) {
    super(ComponentType.BField, x, y, angle);
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
    configs.add(new AbstractModelConfig<Float>(this, 
        Parameter.FieldAngle, 0, 2 * MathUtils.PI) {
      public Float getValue() { return getTheta(); }
      public void setValue(Float angle) { 
        float len = field3.len();
        field3.y = len * MathUtils.sin(angle);
        field3.x = len * MathUtils.cos(angle);
        field3.z = 0;
        getModel().notifyFieldChange();
      }
      public boolean isPossible() { return isActive() && field3.z == 0; }
    });
    
    configs.add(new AbstractModelConfig<String>(this, 
        Parameter.FieldUpDown, Phi.values()) {
      public String getValue() { return field3.z >= 0 ? Phi.Up.name() : Phi.Down.name(); }
      public void setValue(String value) { 
        Phi phi = Phi.valueOf(value); 
        field3.z = phi == Phi.Up ? field3.len() : -field3.len();
        field3.x = field3.y = 0;
        getModel().notifyFieldChange();
      }
      public boolean isPossible() { return isActive() && field3.y == 0 && field3.x == 0; }
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

  public float getTheta() {
    if (field3.z != 0) return 0;
    field2.set(field3.x, field3.y);
    return field2.angle() * MathUtils.degreesToRadians;
  }

  public String getPhi() {
    return field3.z > 0 ? Phi.Up.name() : Phi.Down.name();
  }
}
