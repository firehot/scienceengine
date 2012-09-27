// Copyright 2002-2011, University of Colorado

package com.mazalearn.scienceengine.experiments.electromagnetism.model;

import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.model.ICurrent;
import com.mazalearn.scienceengine.core.model.Science2DBody;

/**
 * CurrentSource is the model of an AC or DC Current source.
 * <p>
 * The Current source has a configurable maximum current. A client varies the
 * maximum current and frequency. The current amplitude varies over
 * time for AC. The direction of current can be changed for DC.
 * 
 * @author sridhar
 */
public class CurrentSource extends Science2DBody implements ICurrent.Source {

  // The minimum number of steps used to approximate one sine wave cycle.
  private static final float MIN_STEPS_PER_CYCLE = 10;

  private static final float DEFAULT_MAX_CURRENT = 5f;

  protected static final float TOLERANCE = 0.01f;
  
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
  private boolean isDCPositive = true;

  /**
   * Sole constructor.
   */
  public CurrentSource(String name, float x, float y, float angle) {
    super(ComponentType.CurrentSource, name, x, y, angle);
    this.frequency = 1.0f; // fastest
    this.acAngle = 0.0f; // radians
    this.deltaAngle = (float) ((2 * Math.PI * this.frequency) / MIN_STEPS_PER_CYCLE); // radians
    this.maxCurrent = DEFAULT_MAX_CURRENT;
    this.current = this.maxCurrent;
    this.initializeConfigs();
  }

  public void initializeConfigs() {
    configs.add(new AbstractModelConfig<String>(getName() + " Type", 
        "Type of Current", CurrentType.values()) {
      public String getValue() { return getCurrentType(); }
      public void setValue(String value) { setCurrentType(value); }
      public boolean isPossible() { return isActive(); }
    });
    
    configs.add(new AbstractModelConfig<Float>(getName() + " Frequency", 
        "Frequency of AC", 0f, 1f) {
      public Float getValue() { return getFrequency(); }
      public void setValue(Float value) { setFrequency(value); }
      public boolean isPossible() { return isActive() && currentType == CurrentType.AC; }
    });
    
    configs.add(new AbstractModelConfig<Float>(getName() + " Max", 
        "Max Current of AC", 0f, DEFAULT_MAX_CURRENT) {
      public Float getValue() { return getMaxCurrent(); }
      public void setValue(Float value) { setMaxCurrent(value); }
      public boolean isPossible() { return isActive(); }
    });
    
    configs.add(new AbstractModelConfig<String>(getName() + " Flip Direction", 
        "Direction of DC Current") {
      public void doCommand() { flipDirection(); }
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
    this.deltaAngle = (float) ((2 * Math.PI * this.frequency) / MIN_STEPS_PER_CYCLE);
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
   * Varies the current over time, based on maxCurrent and frequency.
   * Guaranteed to hit all peaks and zero crossings.
   */
  @Override
  public void singleStep(float dt) {
    if (currentType == CurrentType.DC) {
      setCurrent(isDCPositive ? maxCurrent : -maxCurrent);
      return;
    }

    if (maxCurrent == 0) {
      setCurrent(0.0f);
      return;
    }
    
    // Compute the acAngle.
    this.acAngle += (dt * this.deltaAngle);

    // Limit the acAngle to 360 degrees.
    if (this.acAngle >= 2 * Math.PI) {
      this.acAngle = (float) (this.acAngle % (2 * Math.PI));
    }
    // Calculate and set the amplitude.
    setCurrent(maxCurrent * (float) Math.sin(this.acAngle));
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
   * Sets the maximum voltage that this voltage source will produce.
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
  public void flipDirection() {
    this.isDCPositive = !isDCPositive;
  }
}
