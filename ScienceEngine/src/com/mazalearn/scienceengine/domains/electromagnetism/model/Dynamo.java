// Copyright 2012, Maza Learn Pvt. Ltd.

package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.model.ICurrent;
import com.mazalearn.scienceengine.core.model.Science2DBody;

/**
 * Dynamo is a loop of wire in which current is induced
 * 
 * @author sridhar
 */
public class Dynamo extends Science2DBody implements ICurrent.Source {
  private static final float TOLERANCE = 0.01f;
  private static final float SCALE_OUTPUT = 1E-5f;
  // Dimensions of the coil.
  private float width, height;
  // Current in the wire
  private float current;
  // Terminals
  private Vector2 firstTerminal = new Vector2(), secondTerminal = new Vector2();
  private AreaOrientation areaOrientation = AreaOrientation.ParallelToRotation;
  private int numberOfLoops = 1;
  private float minWidth = 16f;
  private float maxCurrent;

  public enum AreaOrientation {
    ParallelToRotation, PerpendicularToRotation
  }
  
  public Dynamo(float x, float y, float angle) {
    super(ComponentType.Dynamo, x, y, angle);
    this.width = 16f;
    this.height = 16f;
    FixtureDef fixtureDef = new FixtureDef();
    PolygonShape rectangleShape = new PolygonShape();
    rectangleShape.setAsBox(width/2, height/2);
    fixtureDef.density = 1;
    fixtureDef.shape = rectangleShape;
    fixtureDef.filter.categoryBits = 0x0000;
    fixtureDef.filter.maskBits = 0x0000;
    this.createFixture(fixtureDef);
    rectangleShape.dispose();
  }

  @Override
  public void initializeConfigs() {
    super.initializeConfigs();
    configs.add(new AbstractModelConfig<String>(this, 
        Parameter.AreaOrientation, AreaOrientation.values()) {
      public String getValue() { return getAreaOrientation(); }
      public void setValue(String value) { setAreaOrientation(value); }
      public boolean isPossible() { return isActive(); }
    });
    configs.add(new AbstractModelConfig<Float>(this, 
        Parameter.Width, 10f, 16f) {
      public Float getValue() { return getWidth(); }
      public void setValue(Float value) {
        setWidth(value); setHeight(value); 
      }
      public boolean isPossible() { return isActive(); }
    });
    configs.add(new AbstractModelConfig<Float>(this, 
        Parameter.CoilLoops, 1f, 3f) {
      public Float getValue() { return getNumberOfLoops(); }
      public void setValue(Float value) { setNumberOfLoops(value); }
      public boolean isPossible() { return isActive(); }
    });
  }
  
  public void setMagnetFlux(float magnetFlux) {
    float current = 0;
    if (areaOrientation == AreaOrientation.ParallelToRotation) {
      current = maxCurrent = SCALE_OUTPUT * magnetFlux * numberOfLoops; 
    } else {
      float area = getWidth() * getHeight();
      float angle = getBody().getAngle();
      current = SCALE_OUTPUT * magnetFlux * area;
      // Approximation of maxCurrent for simplicity
      maxCurrent = current * numberOfLoops;
      float scale = 1;
      for (int i = 0; i < numberOfLoops; i++) {
        scale += MathUtils.sin(angle);
        angle += MathUtils.PI / numberOfLoops;
      }
      current *= scale;
    }
    current = (float) Math.log(1 + current);
    setCurrent(current);
  }
  
  /**
   * Sets the magnitude of current in the wire. 
   * @param current the current
   */
  private void setCurrent(float current) {
    if (Math.abs(this.current - current) > TOLERANCE) {
      this.current = current;
      getModel().notifyCurrentChange(this);
    }
  }
  
  /**
   * Gets the current in the dynamo coil
   * @return the current
   */
  public float getCurrent() {
    return current;
  }

  /**
   * Gets the peak current in the dynamo coil
   * @return the max current
   */
  public float getMaxCurrent() {
    return maxCurrent;
  }

  public float getWidth() {
    return width;
  }

  public void setWidth(float width) {
    if (width >= minWidth) {
      this.width = width;
    }
  }

  public void setHeight(float height) {
    if (height >= minWidth) {
      this.height = height;
    }
  }

  public float getHeight() {
    return height;
  }

  /**
   * Sets the number of loops in the coil. This method destroys any existing
   * loops and creates a new set.
   * 
   * @param numberOfLoops
   *          the number of loops - must be > 0
   */
  public void setNumberOfLoops(float numberOfLoops) {
    this.numberOfLoops = Math.round(numberOfLoops);
  }

  /**
   * Gets the number of loops in the coil.
   * 
   * @return the number of loops
   */
  public float getNumberOfLoops() {
    return this.numberOfLoops;
  }

  @Override
  public Vector2 getT2Position() {
    if (areaOrientation == AreaOrientation.ParallelToRotation) {
      return firstTerminal.set(getPosition()).add(getWidth()/2, 0);
    }
    return firstTerminal.set(getPosition()).add(+0.5f, 0);
  }

  @Override
  public Vector2 getT1Position() {
    if (areaOrientation == AreaOrientation.ParallelToRotation) {
      return firstTerminal.set(getPosition()).add(0, getWidth()/2);
    }
    return secondTerminal.set(getPosition()).add(-1f, 0);
  }

  public String getAreaOrientation() {
    return areaOrientation.name();
  }

  public void setAreaOrientation(String areaOrientation) {
    this.areaOrientation = AreaOrientation.valueOf(areaOrientation);
  }

  public void setMinWidth(float minWidth) {
    this.minWidth = minWidth;
  }

}

