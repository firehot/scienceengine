package com.mazalearn.scienceengine.guru;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.loaders.ComponentLoader;
import com.mazalearn.scienceengine.app.services.loaders.ConfigLoader;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.ComponentType;
import com.mazalearn.scienceengine.core.model.Parameter;

public abstract class AbstractTutor extends Group implements ITutor {

  protected Array<?> components;
  protected Array<?> configs;
  private int deltaFailureScore;
  private int deltaSuccessScore;
  protected String[] hints;
  private String goal;
  protected IScience2DController science2DController;

  public AbstractTutor(IScience2DController science2DController,
      String goal, Array<?> components, Array<?> configs, 
      int deltaSuccessScore, int deltaFailureScore) {
    this.science2DController = science2DController;
    this.goal = goal;
    this.components = components;
    this.configs = configs;
    this.deltaSuccessScore = deltaSuccessScore;
    this.deltaFailureScore = deltaFailureScore;
  }

  @Override
  public String getGoal() {
    return goal;
  }

  @Override
  public void doSuccessActions() {
  }

  @Override
  public abstract void activate(boolean activate);

  @Override
  public void reinitialize(float x, float y, float width, float height, boolean probeMode) {
    this.setPosition(x, y);
    this.setSize(width, height);
    new ComponentLoader(science2DController).loadComponents(components, false);
    ConfigLoader.loadConfigs(configs, science2DController.getModel());
    science2DController.getControlPanel().refresh();
    // Mark start of tutor in event log
    ScienceEngine.getEventLog().logEvent(ComponentType.Global.name(), 
        Parameter.Tutor.name());
  }

  @Override
  public String getHint() {
    if (hints == null || hints.length == 0) return null;
    return hints[MathUtils.random(0, hints.length - 1)];
  }


  @Override
  public int getSuccessScore() {
    return deltaSuccessScore;
  }
  
  @Override
  public int getFailureScore() {
    return deltaFailureScore;
  }
  
  @Override
  public void checkProgress() {
  }
  
  @Override
  public abstract boolean hasSucceeded();

  @Override
  public abstract boolean hasFailed();
}