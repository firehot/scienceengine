package com.mazalearn.scienceengine.guru;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.Profile;
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
  private String[] hints;
  private String goal;
  protected IScience2DController science2DController;
  protected final ITutor parent;
  protected final Guru guru;
  private GroupType groupType = GroupType.None;
  private String id;
  private Profile profile;
  private float timeSpent;
  protected float completionPercent;
  protected boolean isComplete;

  public AbstractTutor(IScience2DController science2DController,
      ITutor parent, String goal, String id, Array<?> components, Array<?> configs, 
      int deltaSuccessScore, int deltaFailureScore, String[] hints) {
    this.parent = parent;
    this.science2DController = science2DController;
    this.goal = goal;
    this.id = id;
    this.components = components;
    this.configs = configs;
    this.deltaSuccessScore = deltaSuccessScore;
    this.deltaFailureScore = deltaFailureScore;
    this.hints = hints;
    this.guru = science2DController.getGuru();
    this.profile = ScienceEngine.getPreferencesManager().getProfile();
    this.timeSpent = profile.getTimeSpent(id);
    this.completionPercent = profile.getCompletionPercent(id);
    Gdx.app.log(ScienceEngine.LOG, id + ", Time spent: " + timeSpent + ", SuccessPercent: " + completionPercent);
    this.setVisible(false);

  }

  @Override
  public void prepareToFinish(boolean success) {
    if (!success) return;
    this.isComplete = true;
    guru.showNextButton(true);
    Gdx.app.log(ScienceEngine.LOG, "Tutor satisfied: " + getGoal());
  }
  
  protected boolean isComplete() {
    return isComplete;
  }

  @Override
  public String getGoal() {
    return goal;
  }
  
  public String getId() {
    return id;
  }
  
  @Override
  public void finish() {
    Gdx.app.log(ScienceEngine.LOG, "done: " + getId() + " isComplete: " + isComplete);
    this.setVisible(false);
    if (isComplete) {
      this.completionPercent = 100;
      profile.setCompletionPercent(id, getCompletionPercent());
    }
    profile.setTimeSpent(id, getTimeSpent());
    guru.setActiveTutor(parent);
    parent.prepareToFinish(isComplete);
    parent.finish();
  }

  protected void setSuccessScore(int score) {
    deltaSuccessScore = score;
  }

  @Override
  public void teach() {
    Gdx.app.log(ScienceEngine.LOG, "Teach: " + getId());
    this.setVisible(true);
  }
  
  @Override
  public ITutor getParentTutor() {
    return parent;
  }
  
  @Override
  public void prepareToTeach(ITutor childTutor) {
    Gdx.app.log(ScienceEngine.LOG, "Prepare to Teach: " + getId());
    new ComponentLoader(science2DController).loadComponents(components, false);
    ConfigLoader.loadConfigs(configs, science2DController.getModel());
    science2DController.getModelControls().refresh();
    
    this.setVisible(false);
    guru.setActiveTutor(this);
    guru.showNextButton(false);
    // Mark start of tutor in event log
    ScienceEngine.getEventLog().logEvent(ComponentType.Global.name(), 
        Parameter.Tutor.name());
  }

  @Override
  public String getHint() {
    if (hints == null || hints.length == 0) return null;
    return hints[MathUtils.random(0, hints.length - 1)];
  }


  public int getSuccessScore() {
    return deltaSuccessScore;
  }
  
  public int getFailureScore() {
    return deltaFailureScore;
  }
  
  @Override
  public void checkProgress() {
  }
  
  @Override
  public GroupType getGroupType() {
    return groupType;
  }
  
  public void setGroupType(GroupType groupType) {
    this.groupType = groupType;
  }

  @Override
  public List<ITutor> getChildTutors() {
    return null;
  }
  
  @Override
  public void addTimeSpent(float delta) {
    this.timeSpent += delta;
  }
  
  @Override
  public float getTimeSpent() {
    return timeSpent;
  }
  
  @Override
  public float getCompletionPercent() {
    return completionPercent;
  }
  
}