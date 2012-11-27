package com.mazalearn.scienceengine.core.model;

import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;

public class DummyBody extends Science2DBody {
  
  float[] parameters = new float[10];
  private ProbeConfig probeConfig1, probeConfig2;
  
  private final class ProbeConfig extends AbstractModelConfig<Float> {
    private int num;

    private ProbeConfig(Science2DBody body, int num) {
      super(body, ComponentType.Dummy, 0f, 10f);
      this.num = num;
    }

    public Float getValue() { return parameters[num]; }

    public void setValue(Float value) { parameters[num] = value; }

    public boolean isPossible() { return true; }

    public boolean isAvailable() { return ScienceEngine.isProbeMode() && attribute != ComponentType.Dummy; }

    public void setConfigAttribute(IComponentType attribute) {
      this.attribute = attribute;
    }
  }

  public DummyBody(float width, float height, float rotation) {
    super(ComponentType.Dummy, width, height, rotation);
  }

  @Override
  public void initializeConfigs() {
    probeConfig1 = new ProbeConfig(null, 4);
    probeConfig2 = new ProbeConfig(null, 5);
    configs.add(probeConfig1);
    configs.add(probeConfig2);
  }
  
  public void setConfigAttribute(IComponentType attribute, float[] points) {
    if (attribute == null) {
      attribute = ComponentType.Dummy;
    }
    probeConfig1.setConfigAttribute(attribute);
    probeConfig1.setValue(points[0]);
    probeConfig2.setConfigAttribute(attribute);
    probeConfig2.setValue(points[1]);
  }
}