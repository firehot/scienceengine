package com.mazalearn.scienceengine.tutor;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
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
  protected ITutor parent;
  protected final TutorHelper tutorHelper;
  private final ITutorType tutorType;
  private final String id;
  protected boolean success;
  protected float[] stats;
  protected State state = State.Constructed;
  private int successPoints;
  private int failurePoints;
  private String[] explanation;
  private String[] refs;
  private int hintCounter = -1;

  /**
   * State Machine
   * construted --> initialized --> preparedToTeach --> teaching --> systemFinished--> finished --
   *                                      |              \----> userFinished -------/            |
   *                                      |                                                      |
   *                                      |----<-----------------------<----------------------<--|
   * @param science2DController 
   * @param tutorType
   * @param topic - this is the topic of the tutor. But it may be loaded for review in other topics
   * @param parent
   * @param goal
   * @param id
   * @param components
   * @param configs
   * @param hints
   * @param refs 
   * @param explanationImg 
   */
  public AbstractTutor(IScience2DController science2DController,
      ITutorType tutorType, Topic topic, ITutor parent, String goal, 
      String localId, Array<?> components, Array<?> configs, 
      String[] hints, String[] explanation, String[] refs) {
    this.tutorType = tutorType;
    this.parent = parent;
    this.science2DController = science2DController;
    this.goal = goal;
    this.id = makeGlobalId(topic, localId);
    this.components = components;
    this.configs = configs;
    this.successPoints = tutorType.getSuccessPoints();
    this.failurePoints = tutorType.getFailurePoints();
    this.hints = hints;
    this.explanation = explanation;
    // Refs are also using localid
    this.refs = new String[refs.length];
    for (int i = 0; i < refs.length; i++) {
      this.refs[i] = makeGlobalId(topic, refs[i]);
    }
    this.tutorHelper = science2DController.getGuru().getTutorHelper();
    this.stats = tutorHelper.getProfile().getStats(topic, this.id);
    this.setVisible(false);
  }

  private static String makeGlobalId(Topic topic, String localTutorId) {
    return String.valueOf(topic.getTopicId()) + "$" + localTutorId;
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
      tutorHelper.showNextAndExplanation(true, true);
    }
  }
  
  @Override
  public String[] getExplanation() {
    return explanation;
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
    tutorHelper.showNextAndExplanation(false, false);
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
    if (success) stats[ITutor.NUM_SUCCESSES]++;
    recordStats();
    tutorHelper.showNextAndExplanation(false, false);
    tutorHelper.setActiveTutor(parent);
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
  
  @Override
  public void recordStats() {
    // Update all stats
    stats[ITutor.PERCENT_PROGRESS] = stats[ITutor.NUM_SUCCESSES] == 0 ? 0 : 100;   
    parent.recordStats();
    tutorHelper.getProfile().saveStats(stats, id);
  }
  
  @Override
  public float[] getStats() {
    return stats;
  }

  protected void setSuccessPoints(int points) {
    this.successPoints = points;
  }

  @Override
  public void teach() {
    Gdx.app.log(ScienceEngine.LOG, "Teach: " + getId());
    if (getChildTutors() == null) { // Leaf tutor
      ScienceEngine.getSoundManager().play(ScienceEngineSound.CHIME);
    }
    this.setVisible(true);
    success = false;
    this.stats[ITutor.NUM_ATTEMPTS]++;
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
    tutorHelper.setActiveTutor(this);
    tutorHelper.showNextAndExplanation(false, false);
    // Zero out points
    stats[ITutor.POINTS] = 0;
    recordStats();
    // Mark start of tutor in event log
    ScienceEngine.getEventLog().logEvent(ComponentType.Global.name(), 
        Parameter.Tutor.name());
  }

  @Override
  public String getHint() {
    if (hints == null || hints.length == 0) return null;
    hintCounter = (hintCounter + 1) % hints.length;
    return hints[hintCounter];
  }


  public int getSuccessPoints() {
    return tutorHelper.isRevisionMode() ? 0 : successPoints;
  }
  
  public int getFailurePoints() {
    return tutorHelper.isRevisionMode() ? 0 : failurePoints;
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
    this.stats[ITutor.TIME_SPENT] += delta;
  }
  
  @Override
  public State getState() {
    return state;
  }
  
  @Override
  public void setParentTutor(ITutor parentTutor) {
    this.parent = parentTutor;
  }
  
  @Override
  public String[] getRefs() {
    return refs;
  }
  
  @Override
  public String getProgressText() {
    return "1 of 1";
  }
}