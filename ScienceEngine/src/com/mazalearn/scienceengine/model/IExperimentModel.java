package com.mazalearn.scienceengine.model;

import java.util.List;

import com.badlogic.gdx.physics.box2d.World;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.controller.IModelConfig;

public interface IExperimentModel {
  // Reset model to initial conditions
  public void reset();
  // Simulate steps of the model. delta is time since last invocation.
  public void simulateSteps(float delta);
  // Get all configs of the model
  public List<IModelConfig<?>> getAllConfigs();
  // Get a specific named config of the model
  public IModelConfig<?> getConfig(String name);
  public World getBox2DWorld();
  // enable (or disable) the model to progress in simulate steps
  public void enable(boolean enable);
  // whether model is enabled
  public boolean isEnabled();
  public void notifyCurrentChange(ICurrentSource currentSource);
}
