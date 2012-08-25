package com.mazalearn.scienceengine.experiments.model;

import java.util.List;

import com.mazalearn.scienceengine.experiments.controller.IConfig;

public interface IExperimentModel {
  public void reset();
  public void simulateSteps(int n);
  @SuppressWarnings("rawtypes")
  public List<IConfig> getConfigs();
  @SuppressWarnings("rawtypes")
  public IConfig getConfig(String name);
}
