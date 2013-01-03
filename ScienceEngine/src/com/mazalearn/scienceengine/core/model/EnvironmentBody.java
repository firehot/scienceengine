package com.mazalearn.scienceengine.core.model;

import java.util.ArrayList;
import java.util.List;

import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.controller.IModelConfig;

public class EnvironmentBody extends Science2DBody {
  
  List<Float> parameters;
  private List<IModelConfig<?>> environmentConfigs;
  
  private class Parameter implements IParameter {
    private String name;

    private Parameter(String name) {
      this.name = name;
    }
    
    public String toString() {
      return ScienceEngine.getMsg().getString("Name." + name());  
    }
    
    @Override
    public String name() {
      return name;
    }
  }

  public EnvironmentBody(float width, float height, float rotation) {
    super(ComponentType.Environment, width, height, rotation);
    parameters = new ArrayList<Float>();
    environmentConfigs = new ArrayList<IModelConfig<?>>();
  }

  @Override
  public void initializeConfigs() {
    super.initializeConfigs();
    configs.addAll(environmentConfigs);
  }
  
  public void addParameter(String parameterName) {
    Parameter parameter = new Parameter(parameterName);
    final int index = parameters.size();
    parameters.add(5f);
    IModelConfig<Float> parameterConfig = new AbstractModelConfig<Float>(this, 
        parameter, 0f, 10f) {
      public Float getValue() { return parameters.get(index); }
      public void setValue(Float value) { parameters.set(index, value); }
      public boolean isPossible() { return true; }
    };
    environmentConfigs.add(parameterConfig);
  }
}