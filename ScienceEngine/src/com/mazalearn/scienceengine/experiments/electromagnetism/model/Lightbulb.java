// Copyright 2002-2011, University of Colorado

package com.mazalearn.scienceengine.experiments.electromagnetism.model;

import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mazalearn.scienceengine.core.model.ICurrent;
import com.mazalearn.scienceengine.core.model.Science2DBody;

/**
 * Lightbulb is the model of a lightbulb. Its intensity is a function of the
 * current in the pickup coil.
 * 
 * @author sridhar
 */
public class Lightbulb extends Science2DBody implements ICurrent.Sink {

  /* Absolute current amplitude below this value is treated as zero. */
  public static final double CURRENT_AMPLITUDE_THRESHOLD = 0.01;
  // ----------------------------------------------------------------------------
  // Instance data
  // ----------------------------------------------------------------------------

  private float previousCurrent;
  private float current;
  private float intensity;

  // ----------------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------------

  /**
   * Sole constructor.
   * 
   * @param pickupCoilModel - the pickup coil that the lightbulb is across
   */
  public Lightbulb(String name, float x, float y, float angle) {
    super(ComponentType.Lightbulb, name, x, y, angle);

    this.previousCurrent = 0f;
    FixtureDef fixtureDef = new FixtureDef();
    CircleShape circleShape = new CircleShape();
    circleShape.setRadius(6);
    fixtureDef.density = 1;
    fixtureDef.shape = circleShape;
    fixtureDef.filter.categoryBits = 0x0000;
    fixtureDef.filter.maskBits = 0x0000;
    this.createFixture(fixtureDef);
    circleShape.dispose();
  }

  /**
   * Gets the intensity of the light. Fully off is 0.0, fully on is 1.0.
   * 
   * @return the intensity (0.0 - 1.0)
   */
  public float getIntensity() {
    return intensity;
  }

  private void setIntensity() {
    // If current changed angle, turn the light off.
    if (Math.signum(current) != Math.signum(previousCurrent)) {
      intensity = 0f;
    } else if (Math.abs(current)  < CURRENT_AMPLITUDE_THRESHOLD){
      // Intensity below the threshold is effectively zero.
      intensity = 0f;
    } else {
      // Light intensity is proportional to amplitude of current in the coil.
      intensity = current;
    }

    previousCurrent = current;
  }

  @Override
  public void setCurrent(float current) {
    if (this.current != current) {
      this.current = current;
      setIntensity();
    }
  }
}