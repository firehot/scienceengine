package com.mazalearn.scienceengine.experiments.model;

import java.util.List;

import com.mazalearn.scienceengine.experiments.controller.IModelConfig;

public interface IExperimentModel {
  public void reset();
  public void simulateSteps(int n);
  @SuppressWarnings("rawtypes")
  public List<IModelConfig> getConfigs();
  @SuppressWarnings("rawtypes")
  public IModelConfig getConfig(String name);
}
