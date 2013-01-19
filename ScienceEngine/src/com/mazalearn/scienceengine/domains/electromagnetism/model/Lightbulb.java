// Copyright 2002-2011, University of Colorado

package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
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

  private float previousCurrent;
  private float current;
  private float intensity;
  // Terminals
  private Vector2 firstTerminal = new Vector2(), secondTerminal = new Vector2();

  public enum BulbColor {
    Yellow(Color.YELLOW), Red(Color.RED), Blue(Color.BLUE), Green(Color.GREEN);
    
    private Color color;
    
    private BulbColor(Color color) {
      this.color = color;
    }
  }
  BulbColor bulbColor = BulbColor.Yellow;
  /**
   * Sole constructor.
   * 
   * @param pickupCoilModel - the pickup coil that the lightbulb is across
   */
  public Lightbulb(float x, float y, float angle) {
    super(ComponentType.Lightbulb, x, y, angle);

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

  @Override
  public void initializeConfigs() {
    super.initializeConfigs();
    configs.add(new AbstractModelConfig<String>(this, 
        Parameter.Color, BulbColor.values()) {
      public String getValue() { return getBulbColor(); }
      public void setValue(String value) { setBulbColor(value); }
      public boolean isPossible() { return isActive(); }
    });
  }
  
  public String getBulbColor() {
    return bulbColor.name();
  }
  
  public void setBulbColor(String pColor) {
    this.bulbColor = BulbColor.valueOf(pColor);
  }
  
  public Color getColor() {
    return bulbColor.color;
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
  
  @Override
  public Vector2 getT2Position() {
    return firstTerminal.set(getPosition()).add(-0.5f, -2.75f);
  }

  @Override
  public Vector2 getT1Position() {
    return secondTerminal.set(getPosition()).add(0.5f, -3.75f);
  }
  
}