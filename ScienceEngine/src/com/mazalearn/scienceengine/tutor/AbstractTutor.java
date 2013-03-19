package com.mazalearn.scienceengine.tutor;

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

  protected final Array<?> components;
  protected final Array<?> configs;
  private final String[] hints;
  private final String goal;
  protected IScience2DController science2DController;
  protected final ITutor parent;
  protected final Guru guru;
  private final ITutorType tutorType;
  private final String id;
  protected boolean success;
  protected TutorStats stats;
  protected State state = State.Constructed;
  private int successPoints;
  private int failurePoints;

  /**
   * State Machine
   * construted --> initialized --> preparedToTeach --> teaching --> systemFinished--> finished --
   *                                      |              \----> userFinished -------/            |
   *                                      |                                                      |
   *                                      |----<-----------------------<----------------------<--|
   * @param science2DController 
   * @param tutorType2 
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
      ITutorType tutorType, ITutor parent, String goal, String id, Array<?> components, Array<?> configs, 
      int successPoints, int failurePoints, String[] hints) {
    this.tutorType = tutorType;
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
    this.stats = new TutorStats(id);
    this.setVisible(false);

  }

  @Override
  public void systemReadyToFinish(boolean success) {
    if (state == State.SystemFinished && this.success == success) return;
    Gdx.app.log(ScienceEngine.LOG, "System ready to finish: " + getGoal());
    this.success = success;
    if (state == State.UserFinished) {
      state = State.Finished;
      finish();
    } else {
      state = State.SystemFinished;
      guru.showNextButton(true);
    }
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
    if (state == State.SystemFinished) {
      state = State.Finished;
      finish();
    } else {
      state = State.UserFinished;
    }
  }
  
  @Override
  public void finish() {
    Gdx.app.log(ScienceEngine.LOG, "finish: " + getId());
    this.setVisible(false);
    if (success) stats.numSuccesses++;
    recordStats();
    guru.setActiveTutor(parent);
    if (state == State.Finished) { 
      parent.systemReadyToFinish(true);
    } else if (state == State.Aborted) {
      parent.abort();
    } else {
      parent.finish();
    }
  }

  @Override
  public void abort() {
    this.state = State.Aborted;
    finish();
  }
  private void recordStats() {
    // Update all stats
    stats.timeSpent = getTimeSpent();
    stats.numAttempts = getNumAttempts();
    stats.numSuccesses = getNumSuccesses();
    stats.failureTracker = getFailureTracker();
    stats.percentProgress = getPercentProgress();
    
    stats.save();
  }

  protected void setSuccessPoints(int points) {
    this.successPoints = points;
  }

  @Override
  public void teach() {
    Gdx.app.log(ScienceEngine.LOG, "Teach: " + getId());
    this.setVisible(true);
    success = false;
    this.stats.numAttempts++;
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
    if (getChildTutors() == null) {
      science2DController.getModelControls().refresh();
    }
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


  public int getSuccessPoints() {
    return successPoints;
  }
  
  public int getFailurePoints() {
    return failurePoints;
  }
  
  @Override
  public void checkProgress() {
  }
  
  @Override
  public ITutorType getType() {
    return tutorType;
  }
  
  @Override
  public List<ITutor> getChildTutors() {
    return null;
  }
  
  @Override
  public void addTimeSpent(float delta) {
    this.stats.timeSpent += delta;
  }
  
  @Override
  public float getTimeSpent() {
    return stats.timeSpent;
  }
  
  @Override
  public float getNumAttempts() {
    return stats.numAttempts;
  }
  
  @Override
  public float getNumSuccesses() {
    return stats.numSuccesses;
  }
  
  @Override
  public float getPercentProgress() {
    return stats.numSuccesses == 0 ? 0 : 100;
  }
  
  @Override
  public float getFailureTracker() {
    return stats.failureTracker;
  }
  
  @Override
  public State getState() {
    return state;
  }
  
}