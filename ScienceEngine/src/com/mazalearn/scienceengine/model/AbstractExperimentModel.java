package com.mazalearn.scienceengine.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.controller.IModelConfig;

public abstract class AbstractExperimentModel implements IExperimentModel {

  protected World box2DWorld;
  protected List<ScienceBody> bodies = new ArrayList<ScienceBody>(); 
  private List<IModelConfig<?>> modelConfigs;

  private boolean isEnabled = true;
  protected int numStepsPerView = 1;

  public AbstractExperimentModel() {
    super();
    // Initialize the world for Box2D
    Vector2 gravity = new Vector2(0.0f, 0.0f);
    boolean doSleep = true;
    box2DWorld = new World(gravity, doSleep);
    ScienceBody.setBox2DWorld(box2DWorld);    
  }

  @Override
  public void simulateSteps(float delta) {
    if (!isEnabled) return;
    for (int i = 0; i < numStepsPerView; i++) {
      singleStep();
    }
  }

  protected abstract void singleStep();

  public List<IModelConfig<?>> getAllConfigs() {
    List<IModelConfig<?>> allConfigs = new ArrayList<IModelConfig<?>>();
    if (modelConfigs == null) {
      modelConfigs = new ArrayList<IModelConfig<?>>();
      initializeConfigs(modelConfigs);
    }
    allConfigs.addAll(modelConfigs);
    for (ScienceBody body: bodies) {
      if (body.isActive()) {
        allConfigs.addAll(body.getConfigs());
      }
    }
    return Collections.unmodifiableList(allConfigs);
  }
  
  public IModelConfig<?> getConfig(String name) {
    for (IModelConfig<?> modelConfig: modelConfigs) {
      if (modelConfig.getName().equals(name)) return modelConfig;
    }
    return null;
  }

  @Override
  public boolean isEnabled() {
    return isEnabled;
  }
  
  @Override
  public void enable(boolean enable) {
    isEnabled  = enable;
  }
  
  public World getBox2DWorld() {
    return box2DWorld;
  }

  public abstract void initializeConfigs(List<IModelConfig<?>> modelConfigs);
}