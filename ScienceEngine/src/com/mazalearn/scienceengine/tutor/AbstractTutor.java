package com.mazalearn.scienceengine.tutor;

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
  private int failurePoints;
  private int successPoints;
  private String[] hints;
  private String goal;
  protected IScience2DController science2DController;
  protected final ITutor parent;
  protected final Guru guru;
  private GroupType groupType = GroupType.None;
  private String id;
  private Profile profile;
  private float timeSpent;
  protected float attemptPercent;
  protected int numAttempts;
  protected boolean success;

  public AbstractTutor(IScience2DController science2DController,
      ITutor parent, String goal, String id, Array<?> components, Array<?> configs, 
      int successPoints, int failurePoints, String[] hints) {
    this.parent = parent;
    this.science2DController = science2DController;
    this.goal = goal;
    this.id = id;
    this.components = components;
    this.configs = configs;
    this.successPoints = successPoints;
    this.failurePoints = failurePoints;
    this.hints = hints;
    this.guru = science2DController.getGuru();
    this.profile = ScienceEngine.getPreferencesManager().getProfile();
    this.timeSpent = profile.getTimeSpent(id);
    this.attemptPercent = profile.getPercentAttempted(id);
    Gdx.app.log(ScienceEngine.LOG, id + ", Time spent: " + timeSpent + ", SuccessPercent: " + attemptPercent);
    this.setVisible(false);

  }

  @Override
  public void delegateeHasFinished(boolean success) {
    if (!success) return;
    if (getGroupType() == GroupType.None) { 
      this.numAttempts++;
    }
    this.success = success;
    guru.showNextButton(true);
    Gdx.app.log(ScienceEngine.LOG, "Tutor satisfied: " + getGoal());
  }
  
  /**
   * Did this specific instance of the tutor end with success?
   * @return
   */
  protected boolean isSuccess() {
    return success;
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
    Gdx.app.log(ScienceEngine.LOG, "done: " + getId() + " isAttempted: " + numAttempts);
    this.setVisible(false);
    if (numAttempts > 0) {
      this.attemptPercent = 100;
      profile.setPercentAttempted(id, getPercentAttempted());
    }
    profile.setTimeSpent(id, getTimeSpent());
    guru.setActiveTutor(parent);
    parent.delegateeHasFinished(success);
    parent.finish();
  }

  protected void setSuccessPoints(int points) {
    successPoints = points;
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
    return successPoints;
  }
  
  public int getFailurePoints() {
    return failurePoints;
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
  public float getPercentAttempted() {
    return attemptPercent;
  }
  
}