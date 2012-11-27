package com.mazalearn.scienceengine.core.model;

import java.util.ArrayList;
import java.util.List;

import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.AbstractModelConfig;
import com.mazalearn.scienceengine.core.controller.IModelConfig;

public class EnvironmentBody extends Science2DBody {
  
  List<Float> parameters;
  private List<IModelConfig<?>> environmentConfigs;
  
  private class Attribute implements IComponentType {
    private String name;

    private Attribute(String name) {
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
    configs.addAll(environmentConfigs);
  }
  
  public void addParameter(String parameterName) {
    Attribute attribute = new Attribute(parameterName);
    final int index = parameters.size();
    parameters.add(5f);
    IModelConfig<Float> attributeConfig = new AbstractModelConfig<Float>(this, 
        attribute, 0f, 10f) {
      public Float getValue() { return parameters.get(index); }
      public void setValue(Float value) { parameters.set(index, value); }
      public boolean isPossible() { return true; }
    };
    environmentConfigs.add(attributeConfig);
  }
}