package com.mazalearn.scienceengine.guru;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
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
  private List<String> goals = new ArrayList<String>();
  protected IScience2DController science2DController;
  private final ITutor parent;
  private Boolean isActivated = null; 

  public AbstractTutor(IScience2DController science2DController,
      ITutor parent, String goal, Array<?> components, Array<?> configs, 
      int deltaSuccessScore, int deltaFailureScore) {
    this.parent = parent;
    this.science2DController = science2DController;
    this.goals.add(goal);
    this.components = components;
    this.configs = configs;
    this.deltaSuccessScore = deltaSuccessScore;
    this.deltaFailureScore = deltaFailureScore;
  }

  @Override
  public String getGoal() {
    return goals.get(goals.size() - 1);
  }

  @Override
  public void pushGoal(String goal) {
    if (parent != null) {
      parent.pushGoal(goal);
    } else {
      goals.add(goal);
    }
  }
  
  @Override
  public void popGoal() {
    if (parent != null) {
      parent.popGoal();
    } else {
      goals.remove(goals.size() - 1);
    }
  }
  
  @Override
  public void doSuccessActions() {
  }

  @Override
  public void activate(boolean activate) {
    if (activate) {
      pushGoal(goals.get(0));
    } else if (isActivated != null && isActivated){
      popGoal();
    }
    isActivated = activate;
    this.setVisible(activate);
  }

  @Override
  public void reinitialize(boolean probeMode) {
    if (probeMode) {
      reset();
    }
    // Mark start of tutor in event log
    ScienceEngine.getEventLog().logEvent(ComponentType.Global.name(), 
        Parameter.Tutor.name());
  }

  @Override
  public void reset() {
    Gdx.app.log(ScienceEngine.LOG, "Reset tutor");
    new ComponentLoader(science2DController).loadComponents(components, false);
    ConfigLoader.loadConfigs(configs, science2DController.getModel());
    science2DController.getModelControls().refresh();
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