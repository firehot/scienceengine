package com.mazalearn.scienceengine.model;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.controller.IModelConfig;

public abstract class AbstractExperimentModel implements IExperimentModel {

  protected World box2DWorld;
  @SuppressWarnings("rawtypes")
  protected List<IModelConfig> modelConfigs;

  private boolean isEnabled = true;

  @SuppressWarnings("rawtypes")
  public AbstractExperimentModel() {
    super();
    // Initialize the world for Box2D if not already available
    Vector2 gravity = new Vector2(0.0f, 0.0f);
    boolean doSleep = true;
    box2DWorld = new World(gravity, doSleep);
    ScienceBody.setBox2DWorld(box2DWorld);    
    modelConfigs = new ArrayList<IModelConfig>();
    initializeConfigs();
  }

  @Override
  public void simulateSteps(int n) {
    if (!isEnabled) return;
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

  @Override
  public void enable(boolean enable) {
    isEnabled  = enable;
  }
  
  public World getBox2DWorld() {
    return box2DWorld;
  }

  protected abstract void initializeConfigs();
}