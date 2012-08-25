package com.mazalearn.scienceengine.experiments.model;

import java.util.ArrayList;
import java.util.List;

import com.mazalearn.scienceengine.experiments.controller.IConfig;

public abstract class AbstractExperimentModel implements IExperimentModel {

  @SuppressWarnings("rawtypes")
  protected List<IConfig> configs;

  @SuppressWarnings("rawtypes")
  public AbstractExperimentModel() {
    super();
    configs = new ArrayList<IConfig>();
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
  public List<IConfig> getConfigs() {
    return configs;
  }
  
  @SuppressWarnings({ "rawtypes" })
  public IConfig getConfig(String name) {
    for (IConfig config: configs) {
      if (config.getName() == name) return config;
    }
    return null;
  }

  protected abstract void initializeConfigs();

}