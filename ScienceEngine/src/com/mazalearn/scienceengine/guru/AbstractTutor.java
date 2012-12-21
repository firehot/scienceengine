package com.mazalearn.scienceengine.guru;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.loaders.ComponentLoader;
import com.mazalearn.scienceengine.app.services.loaders.ConfigLoader;
import com.mazalearn.scienceengine.core.model.ComponentType;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.core.view.Parameter;

public abstract class AbstractTutor extends Group implements ITutor{

  protected Array<?> components;
  protected Array<?> configs;
  private int deltaFailureScore;
  private int deltaSuccessScore;
  protected final IScience2DModel science2DModel;
  protected final IScience2DView science2DView;
  protected String[] hints;
  private String goal;

  public AbstractTutor(IScience2DModel science2DModel, IScience2DView science2DView,
      String goal, int deltaSuccessScore, int deltaFailureScore) {
    this.science2DModel = science2DModel;
    this.science2DView = science2DView;
    this.goal = goal;
    this.deltaSuccessScore = deltaSuccessScore;
    this.deltaFailureScore = deltaFailureScore;
  }

  @Override
  public String getGoal() {
    return goal;
  }

  @Override
  public abstract void activate(boolean activate);

  @Override
  public void reinitialize(float x, float y, float width, float height, boolean probeMode) {
    this.setPosition(x, y);
    this.setSize(width, height);
    new ComponentLoader(science2DModel, science2DView).loadComponents(components, false);
    ConfigLoader.loadConfigs(configs, science2DModel);
    // Mark start of tutor in event log
    ScienceEngine.getEventLog().logEvent(ComponentType.Global.name(), 
        Parameter.Tutor.name());
  }

  @Override
  public String getHint() {
    if (hints.length == 0) return null;
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
  public void initialize(Array<?> components, Array<?> configs) {
    this.components = components;
    this.configs = configs;
  }
  
  @Override
  public abstract boolean isCompleted();
}