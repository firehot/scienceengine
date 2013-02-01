// Copyright 2002-2011, University of Colorado

package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.model.ICurrent;
import com.mazalearn.scienceengine.core.model.Science2DBody;

/**
 * CurrentSource is the model of an AC or DC Current source.
 * <p>
 * The Current source has a configurable maximum current. A client varies the
 * maximum current and frequency. The current amplitude varies over
 * timeLimit for AC. The direction of current can be changed for DC.
 * 
 * @author sridhar
 */
public class CurrentSource extends Science2DBody implements ICurrent.Source {

  // The minimum number of steps used to approximate one sine wave cycle.
  private static final float MIN_STEPS_PER_CYCLE = 10;

  public static final float DEFAULT_MAX_CURRENT = 5f;

  private static final float TOLERANCE = 0.01f;
  
  public static enum CurrentType {AC, DC};

  // Determines how fast the current will vary. (0...1 inclusive)
  private float frequency;
  // The current angle of the sine wave that describes the AC. (radians)
  private float acAngle;
  // The change in acAngle at the current frequency. (radians)
  private float deltaAngle;
  // Maximum current peak
  private float maxCurrent;
  // actual current
  private float current;
  // type of current
  private CurrentType currentType = CurrentType.DC;
  // direction of current - applies only to DC.
  private boolean isNegativeCurrent = true;
  // Terminals
  private Vector2 firstTerminal = new Vector2(), secondTerminal = new Vector2();

  /**
   * Sole constructor.
   */
  public CurrentSource(float x, float y, float angle) {
    super(ComponentType.CurrentSource, x, y, angle);
    this.frequency = 1.0f;
    this.acAngle = 0.0f; // radians
    this.deltaAngle = (2 * MathUtils.PI * this.frequency) / MIN_STEPS_PER_CYCLE; // radians
    this.maxCurrent = DEFAULT_MAX_CURRENT;
    this.current = this.maxCurrent;
  }

  @Override
  public void initializeConfigs() {
    super.initializeConfigs();
    configs.add(new AbstractModelConfig<String>(this, 
        Parameter.CurrentType, CurrentType.values()) {
      public String getValue() { return getCurrentType(); }
      public void setValue(String value) { setCurrentType(value); }
      public boolean isPossible() { return isActive(); }
      public boolean hasProbeMode() { return true; }
      public void setProbeMode() { setCurrentType(CurrentType.DC.name()); }
    });
    
    configs.add(new AbstractModelConfig<Float>(this, 
        Parameter.CurrentFrequency, 0f, 4f) {
      public Float getValue() { return getFrequency(); }
      public void setValue(Float value) { setFrequency(value); }
      public boolean isPossible() { return isActive() && currentType == CurrentType.AC; }
    });
    
    configs.add(new AbstractModelConfig<Float>(this, 
        Parameter.MaxCurrent, 0f, DEFAULT_MAX_CURRENT) {
      public Float getValue() { return getMaxCurrent(); }
      public void setValue(Float value) { setMaxCurrent(value); }
      public boolean isPossible() { return isActive(); }
    });
    
    configs.add(new AbstractModelConfig<Float>(this, 
        Parameter.Current, -DEFAULT_MAX_CURRENT, DEFAULT_MAX_CURRENT) {
      public Float getValue() { return getCurrent(); }
      public void setValue(Float value) { setCurrent(value); }
      public boolean isPossible() { return false; /* meter */}
    });

    configs.add(new AbstractModelConfig<Boolean>(this, 
        Parameter.CurrentDirection, false) {
      public void setValue(Boolean value) { setNegativeCurrent(value); }
      public Boolean getValue() { return isNegativeCurrent; }
      public boolean isPossible() { return isActive() && currentType == CurrentType.DC; }
    });
  }

  public void setCurrentType(String value) {
    currentType = CurrentType.valueOf(value);
  }

  public String getCurrentType() {
    return currentType.name();
  }

  /**
   * Sets the frequency.
   * 
   * @param frequency
   *          the frequency, 0...1 inclusive
   */
  public void setFrequency(float frequency) {
    assert (frequency >= 0 && frequency <= 1);
    this.frequency = frequency;
    this.acAngle = 0.0f;
    this.deltaAngle = (2 * MathUtils.PI * this.frequency) / MIN_STEPS_PER_CYCLE;
  }

  /**
   * Gets the frequency.
   * 
   * @return the frequency, 0...1 inclusive
   */
  public float getFrequency() {
    return this.frequency;
  }

  /*
   * Varies the current over timeLimit, based on maxCurrent and frequency.
   * Guaranteed to hit all peaks and zero crossings.
   */
  @Override
  public void singleStep(float dt) {
    if (currentType == CurrentType.DC) {
      setCurrent(isNegativeCurrent ? maxCurrent : -maxCurrent);
      return;
    }

    if (maxCurrent == 0) {
      setCurrent(0.0f);
      return;
    }
    
    // Compute the acAngle.
    this.acAngle += (dt * this.deltaAngle);

    // Limit the acAngle to 360 degrees.
    if (this.acAngle >= 2 * MathUtils.PI) {
      this.acAngle = this.acAngle % (2 * MathUtils.PI);
    }
    // Calculate and set the amplitude.
    setCurrent(maxCurrent * MathUtils.sin(this.acAngle));
  }

  /**
   * Gets the current.
   * 
   * @return the current, in amperes
   */
  public float getCurrent() {
    return this.current;
  }

  /**
   * Sets the current, in amperes - can only be set from within this class
   */
  private void setCurrent(float current) {
    if (Math.abs(this.current - current) > TOLERANCE) {
      this.current = current;
      getModel().notifyCurrentChange(this);
    }
  }

  /**
   * Sets the maximum current that this current source will produce.
   * 
   * @param maxCurrent - the maximum voltage, in volts
   */
  public void setMaxCurrent(float maxCurrent) {
    this.maxCurrent = maxCurrent;
  }

  /**
   * Gets the maximum current that this current source will produce.
   * 
   * @return the maximum current, in amperes
   */
  public float getMaxCurrent() {
    return this.maxCurrent;
  }

  /**
   * Changes the direction of DC current.
   */
  public void setNegativeCurrent(boolean negative) {
    if (this.isNegativeCurrent != negative) {
      this.isNegativeCurrent = negative;
      setCurrent(isNegativeCurrent ? maxCurrent : -maxCurrent);
      getModel().notifyCurrentChange(this);
    }
  }

  @Override
  public Vector2 getT1Position() {
    return firstTerminal.set(getPosition()).add(-1, 1.5f);
  }

  @Override
  public Vector2 getT2Position() {
    return secondTerminal.set(getPosition()).add(-1, -4.5f);
  }
}
