package com.mazalearn.scienceengine.guru;

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
  protected String goal;
  protected IScience2DController science2DController;
  protected final ITutor parent;
  protected Guru guru;
  protected boolean isActive;

  public AbstractTutor(IScience2DController science2DController,
      ITutor parent, String goal, Array<?> components, Array<?> configs, 
      int deltaSuccessScore, int deltaFailureScore) {
    this.parent = parent;
    this.science2DController = science2DController;
    this.goal = goal;
    this.components = components;
    this.configs = configs;
    this.deltaSuccessScore = deltaSuccessScore;
    this.deltaFailureScore = deltaFailureScore;
    this.guru = science2DController.getGuru();
  }

  @Override
  public String getGoal() {
    return goal;
  }
  
  @Override
  public void done(boolean success) {
    activate(false);
    reinitialize(false);
    if (success) {
      doSuccessActions();
    }
    parent.done(success);
  }

  protected void setSuccessScore(int score) {
    deltaSuccessScore = score;
  }

  @Override
  public void doSuccessActions() {
  }

  @Override
  public void activate(boolean activate) {
    // TODO: if (isActive == activate) return;
    isActive = activate;
    if (activate) {
      guru.pushGoal(goal);
    } else {
      guru.popGoal(goal);
    }
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
}