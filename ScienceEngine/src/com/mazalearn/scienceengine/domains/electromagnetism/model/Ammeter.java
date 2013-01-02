package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.model.ICurrent;
import com.mazalearn.scienceengine.core.model.Science2DBody;

/**
 * Ammeter is the model of an analog ammeter. It's needle deflection is a
 * function of the current in the circuit.
 * 
 * @author sridhar
 */
public class Ammeter extends Science2DBody implements ICurrent.Sink {

  /* Absolute current amplitude below this value is treated as zero. */
  public static final float CURRENT_AMPLITUDE_THRESHOLD = 0.001f;

  // The needle deflection range is this much on either side of the zero point.
  private static final float MAX_NEEDLE_ANGLE = MathUtils.degreesToRadians * 90;

  private float maxCurrent = 3;

  private static final int SMOOTH = 4;

  // Terminals
  private Vector2 firstTerminal = new Vector2(), secondTerminal = new Vector2();

  // Needle deflection angle
  private float needleAngle;

  private float current;

  public Ammeter(float x, float y, float angle) {
    super(ComponentType.Ammeter, x, y, angle);
    needleAngle = 0;
  }

  @Override
  public void initializeConfigs() {
    super.initializeConfigs();
    configs.add(new AbstractModelConfig<Float>(this, 
        Parameter.MaxCurrent, 1f, 20f) {
      public Float getValue() { return getMaxCurrent(); }
      public void setValue(Float value) { setMaxCurrent(value); }
      public boolean isPossible() { return isActive(); }
    });
  }
  
  /**
   * Sets the needle's deflection angle.
   * 
   * @param needleAngle - the angle, in radians
   */
  protected void setNeedleAngle(float needleAngle) {
    needleAngle = Clamp.clamp(-MAX_NEEDLE_ANGLE, needleAngle, +MAX_NEEDLE_ANGLE);
    // Smoothe out the needle angle using hysteresis   
    this.needleAngle = (needleAngle + (SMOOTH - 1) * this.needleAngle) / SMOOTH;
  }

  /**
   * @return needle deflection angle, in radians
   */
  public float getNeedleAngle() {
    return needleAngle;
  }

  /**
   * Gets the desired needle deflection angle. This is the angle that
   * corresponds exactly to the voltage read by the meter.
   * 
   * @return the angle, in radians
   */
  private float computeNeedleAngle() {

    // Use amplitude of the current source as our signal.
    float amplitude = current / getMaxCurrent();

    // Absolute amplitude below the threshold is effectively zero.
    if (Math.abs(amplitude) < CURRENT_AMPLITUDE_THRESHOLD) {
      return 0;
    }

    // Determine the needle deflection angle.
    return amplitude * MAX_NEEDLE_ANGLE;
  }

  /*
   * Updates the needle deflection angle.
   */
  public void singleStep(float dt) {
    // Determine the desired needle deflection angle.
    setNeedleAngle(computeNeedleAngle());
  }

  @Override
  public Vector2 getT1Position() {
    return firstTerminal.set(getPosition()).add(5f, -4f);
  }

  @Override
  public Vector2 getT2Position() {
    return secondTerminal.set(getPosition()).add(-5f, -4f);
  }

  @Override
  public void setCurrent(float current) {
    this.current = current;
  }

  public float getMaxCurrent() {
    return maxCurrent;
  }

  public void setMaxCurrent(float maxCurrent) {
    this.maxCurrent = maxCurrent;
  }
}
