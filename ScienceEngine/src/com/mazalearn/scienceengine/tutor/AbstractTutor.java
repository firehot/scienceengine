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
  protected int numAttempts;
  protected boolean success;
  protected State state = State.Constructed;

  /**
   * State Machine
   * construted --> initialized --> preparedToTeach --> teaching --> systemFinished--> finished --
   *                                      |              \----> userFinished -------/            |
   *                                      |                                                      |
   *                                      |----<-----------------------<----------------------<--|
   * @param science2DController 
   * @param parent
   * @param goal
   * @param id
   * @param components
   * @param configs
   * @param successPoints
   * @param failurePoints
   * @param hints
   */
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
    this.numAttempts = (int) profile.getNumAttempts(id);
    Gdx.app.log(ScienceEngine.LOG, id + ", Time spent: " + timeSpent + ", NumAttempts: " + numAttempts);
    this.setVisible(false);

  }

  @Override
  public void systemReadyToFinish(boolean success) {
    if (state == State.SystemFinished && this.success == success) return;
    Gdx.app.log(ScienceEngine.LOG, "System ready to finish: " + getGoal());
    this.success = success;
    state = (state == State.UserFinished) ? State.Finished : State.SystemFinished;
    if (state == State.SystemFinished) {
      guru.showNextButton(true);
    }
    finish();
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
  public void userReadyToFinish() { // Assumed to be always success
    Gdx.app.log(ScienceEngine.LOG, "User has finished");
    guru.showNextButton(false);
    state = (state == State.SystemFinished) ? State.Finished : State.UserFinished;
    finish();
  }
  
  @Override
  public void finish() {
    if (state != State.Finished) return;
    Gdx.app.log(ScienceEngine.LOG, "finish: " + getId());
    this.setVisible(false);
    
    recordStats();
    guru.setActiveTutor(parent);
    parent.systemReadyToFinish(true);
  }

  private void recordStats() {
    profile.setNumAttempts(id, getNumAttempts());
    profile.setTimeSpent(id, getTimeSpent());
  }

  protected void setSuccessPoints(int points) {
    successPoints = points;
  }

  @Override
  public void teach() {
    Gdx.app.log(ScienceEngine.LOG, "Teach: " + getId());
    this.setVisible(true);
    this.numAttempts++;
    state = State.Teaching;
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
    state = State.PreparedToTeach;
    
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
  public float getNumAttempts() {
    return numAttempts;
  }
  
  @Override
  public float getAttemptPercent() {
    return numAttempts == 0 ? 0 : 100;
  }
  
  @Override
  public State getState() {
    return state;
  }
  
}