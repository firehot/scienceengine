// Copyright 2013, Maza Learn Pvt Ltd

package com.mazalearn.scienceengine.domains.electromagnetism.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;

/**
 * Model of a basic magnet.
 */
public class Magnet extends AbstractMagnet {

  // BHMax, Cost are assumed to be for this volume.
  private static final int CANONICAL_WIDTH = 8;
  private static final int CANONICAL_HEIGHT = 2;
  private static final int CANONICAL_AREA = CANONICAL_WIDTH * CANONICAL_HEIGHT;
  
  public enum MagnetType {
    Ferrite(2, 4), Smco(100, 26), Neodymium(50, 41);

    private static final float OUTPUT_SCALE = 0.5f;
    private float costPerLb;
    private float bhMax;

    private float getStrength(float area) {
      return area * bhMax * OUTPUT_SCALE;
    }
    
    private MagnetType(float costPerLb, float bhMax) {
      this.costPerLb = costPerLb;
      this.bhMax = bhMax;
    }
  };
  
  private MagnetType magnetType = MagnetType.Neodymium;
  private float maxWidth = CANONICAL_WIDTH * 2;
  private AbstractModelConfig<Float> strengthConfig;
  private float maxHeight = CANONICAL_HEIGHT * 2;
  
  public Magnet(float x, float y, float angle) {
    super(ComponentType.Magnet, x, y, angle);
    
    this.setSize(CANONICAL_WIDTH, CANONICAL_HEIGHT);
    FixtureDef fixtureDef = new FixtureDef();
    PolygonShape rectangleShape = new PolygonShape();
    rectangleShape.setAsBox(getWidth()/2, getHeight()/2);
    fixtureDef.density = 1;
    fixtureDef.shape = rectangleShape;
    fixtureDef.filter.categoryBits = 0x0001;
    fixtureDef.filter.maskBits = 0x0000;
    this.createFixture(fixtureDef);
    rectangleShape.dispose();
    this.setAngularDamping(0.1f);
  }

  @Override
  public void initializeConfigs() {
    super.initializeConfigs();
    strengthConfig = new AbstractModelConfig<Float>(this, 
        Parameter.MagnetStrength, 0f, 1600f) {
      public Float getValue() { return getStrength(); }
      public void setValue(Float magnetStrength) { setStrength(magnetStrength); }
      public boolean isPossible() { return isActive(); }
    };
    configs.add(strengthConfig);
    configs.add(new AbstractModelConfig<Float>(this, 
        Parameter.Cost, 0f, 1000000f) {
      public Float getValue() { return getWidth() * getHeight() * magnetType.costPerLb; }
      public void setValue(Float value) { /* METER - no set*/ }
      public boolean isPossible() { return isActive(); }
    });
    configs.add(new AbstractModelConfig<String>(this, 
        Parameter.MagnetType, MagnetType.values()) {
      public String getValue() { return getMagnetType(); }
      public void setValue(String value) { setMagnetType(value); }
      public boolean isPossible() { return isActive(); }
    });
  }
  
  @Override
  public void reset() {
    // Set strength of magnet in accordance with area
    strengthConfig.setRange(strengthConfig.getLow(), magnetType.getStrength(maxWidth * maxHeight));
    super.setStrength(magnetType.getStrength(getWidth() * getHeight()));
    super.reset();
  }

  @Override
  public void setStrength(float strength) {
    // Actually, should be volume - but we will use area for 2D
    // Strength depends on volume of magnetic material, 
    // Change width, height appropriately - using area instead of volume
    float scale = (float) Math.sqrt(strength / magnetType.getStrength(CANONICAL_AREA));
    if (scale * CANONICAL_WIDTH > maxWidth) {
      // set width to maxWidth and set strength to max possible.
      scale = maxWidth / CANONICAL_WIDTH;
      strength = magnetType.getStrength(maxWidth * maxHeight);
    }
    setSize(scale * CANONICAL_WIDTH, scale * CANONICAL_HEIGHT);
    super.setStrength(strength);
  }
  
  public float getMaxWidth() {
    return maxWidth;
  }

  public void setMaxWidth(float maxWidth) {
    this.maxWidth = maxWidth;
    this.maxHeight = maxWidth * CANONICAL_HEIGHT / CANONICAL_WIDTH;
    strengthConfig.setRange(strengthConfig.getLow(), magnetType.getStrength(maxWidth * maxHeight));
  }

  public String getMagnetType() {
    return magnetType.name();
  }

  public void setMagnetType(String magnetType) {
    this.magnetType = MagnetType.valueOf(magnetType);
    // Adjust size based on strength
    setStrength(getStrength());
    strengthConfig.setRange(strengthConfig.getLow(), this.magnetType.getStrength(maxWidth * maxHeight));
  }

  /**
   * Gets the B-field vector at a point in the magnet's local 2D coordinate
   * frame.
   * 
   * @param p
   * @param outputVector
   * @return outputVector
   */
  protected Vector2 getBFieldRelative(Vector2 p, Vector2 outputVector) {

    assert (p != null);
    assert (outputVector != null);

    outputVector.set(0, 0);

    return outputVector;
  }
}
