package com.mazalearn.scienceengine.model;

import java.util.List;

import com.badlogic.gdx.physics.box2d.World;
import com.mazalearn.scienceengine.controller.IModelConfig;

public interface IExperimentModel {
  public void reset();
  public void simulateSteps();
  public List<IModelConfig<?>> getConfigs();
  public IModelConfig<?> getConfig(String name);
  public World getBox2DWorld();
  public void enable(boolean enable);
  public boolean isEnabled();
}
