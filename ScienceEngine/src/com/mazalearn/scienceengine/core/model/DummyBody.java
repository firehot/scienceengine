package com.mazalearn.scienceengine.core.model;

import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;

public class DummyBody extends Science2DBody {
  
  float[] parameters = new float[10];
  private ProbeConfig probeConfig;
  
  private final class ProbeConfig extends AbstractModelConfig<Float> {
    private int num;

    private ProbeConfig(Science2DBody body, int num) {
      super(body, Parameter.Select, 0f, 10f);
      this.num = num;
    }

    public Float getValue() { return parameters[num]; }

    public void setValue(Float value) { parameters[num] = value; }

    public boolean isPossible() { return true; }

    public boolean isAvailable() { return ScienceEngine.isProbeMode() && parameter != Parameter.Select; }

    public void setConfigParameter(IParameter parameter) {
      this.parameter = parameter;
      setPermitted(parameter != Parameter.Select);
    }
  }

  public DummyBody(float width, float height, float rotation) {
    super(ComponentType.Dummy, width, height, rotation);
  }

  @Override
  public void initializeConfigs() {
    probeConfig = new ProbeConfig(null, 4);
    configs.add(probeConfig);
  }
  
  @Override
  public boolean allowsConfiguration() {
    return true;
  }

  public void setConfigParameter(IParameter parameter, float value) {
    if (parameter == null) {
      parameter = Parameter.Select;
    }
    probeConfig.setConfigParameter(parameter);
    probeConfig.setValue(value);
  }
}