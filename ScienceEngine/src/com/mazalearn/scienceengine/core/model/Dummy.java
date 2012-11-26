package com.mazalearn.scienceengine.core.model;

import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;

public class Dummy extends Science2DBody {
  
  float[] dummies = new float[10];
  private ProbeConfig probeConfig1, probeConfig2;
  
  private final class ProbeConfig extends AbstractModelConfig<Float> {
    private int num;

    private ProbeConfig(Science2DBody body, int num) {
      super(body, DummyType.Dummy, 0f, 10f);
      this.num = num;
    }

    public Float getValue() { return dummies[num]; }

    public void setValue(Float value) { dummies[num] = value; }

    public boolean isPossible() { return true; }

    public boolean isAvailable() { return ScienceEngine.isProbeMode() && attribute != DummyType.Dummy; }

    public void setConfigAttribute(IComponentType attribute) {
      this.attribute = attribute;
    }
  }

  private enum DummyType implements IComponentType {
    Dummy,
    AirPermittivity,
    RoomTemperature,
    EarthMagneticField;
  }

  public Dummy(float width, float height, float rotation) {
    super(DummyType.Dummy, width, height, rotation);
  }

  @Override
  public void initializeConfigs() {
    configs.add(new AbstractModelConfig<Float>(null, 
        DummyType.AirPermittivity, 0f, 4f) {
      public Float getValue() { return dummies[0]; }
      public void setValue(Float value) { dummies[0] = value; }
     public boolean isPossible() { return true; }
    });
    
    configs.add(new AbstractModelConfig<Float>(null, 
        DummyType.RoomTemperature, 0f, 4f) {
      public Float getValue() { return dummies[1]; }
      public void setValue(Float value) { dummies[1] = value; }
      public boolean isPossible() { return true; }
    });
    
    configs.add(new AbstractModelConfig<Float>(null, 
        DummyType.EarthMagneticField, 0f, 4f) {
      public Float getValue() { return dummies[2]; }
      public void setValue(Float value) { dummies[2] = value; }
      public boolean isPossible() { return true; }
    });
    
    probeConfig1 = new ProbeConfig(null, 4);
    probeConfig2 = new ProbeConfig(null, 5);
    configs.add(probeConfig1);
    configs.add(probeConfig2);
  }
  
  public void setConfigAttribute(IComponentType attribute, float[] points) {
    if (attribute == null) {
      attribute = DummyType.Dummy;
    }
    probeConfig1.setConfigAttribute(attribute);
    probeConfig1.setValue(points[0]);
    probeConfig2.setConfigAttribute(attribute);
    probeConfig2.setValue(points[1]);
  }
}