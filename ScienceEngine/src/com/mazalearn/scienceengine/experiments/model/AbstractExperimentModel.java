package com.mazalearn.scienceengine.experiments.model;

import java.util.ArrayList;
import java.util.List;

import com.mazalearn.scienceengine.experiments.controller.IModelConfig;

public abstract class AbstractExperimentModel implements IExperimentModel {

  @SuppressWarnings("rawtypes")
  protected List<IModelConfig> modelConfigs;

  @SuppressWarnings("rawtypes")
  public AbstractExperimentModel() {
    super();
    modelConfigs = new ArrayList<IModelConfig>();
    initializeConfigs();
  }

  @Override
  public void simulateSteps(int n) {
    for (int i =0; i < n; i++) {
      singleStep();
    }
  }

  protected abstract void singleStep();

  @SuppressWarnings({ "rawtypes" })
  public List<IModelConfig> getConfigs() {
    return modelConfigs;
  }
  
  @SuppressWarnings({ "rawtypes" })
  public IModelConfig getConfig(String name) {
    for (IModelConfig modelConfig: modelConfigs) {
      if (modelConfig.getName() == name) return modelConfig;
    }
    return null;
  }

  protected abstract void initializeConfigs();

}