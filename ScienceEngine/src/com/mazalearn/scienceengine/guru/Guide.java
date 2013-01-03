package com.mazalearn.scienceengine.guru;

import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.IScience2DView;

// outcome = function of parameter
// doubts on how parameter change affects magnitude of outcome
// Generate A, B as two parameter points.
// Is the outcome stronger at A or B?
public class Guide extends AbstractTutor {
  
  private List<Subgoal> subgoals = Collections.emptyList();
  
  private float[] stageBeginTime;
  private int currentStage = -1;

  private float guruWidth;
  private float guruHeight;
    
  public Guide(IScience2DModel science2DModel, IScience2DView science2DView,
      String goal, Array<?> components, Array<?> configs, int deltaSuccessScore, int deltaFailureScore) {
    super(science2DModel, science2DView, goal, components, configs, deltaSuccessScore, deltaFailureScore);
  }
  
  /* (non-Javadoc)
   * @see com.mazalearn.scienceengine.guru.AbstractTutor#activate(boolean)
   */
  @Override
  public void activate(boolean activate) {
    if (activate) {
      science2DView.getGuru().setSize(guruWidth,  50);
      science2DView.getGuru().setPosition(0, guruHeight - 50);
      stageBeginTime[currentStage] = ScienceEngine.getTime();
    } 
    ScienceEngine.setProbeMode(false);
    this.setVisible(activate);
  }

  /* (non-Javadoc)
   * @see com.mazalearn.scienceengine.guru.AbstractTutor#reinitialize(float, float, float, float)
   */
  @Override
  public void reinitialize(float x, float y, float width, float height, boolean probeMode) {
    super.reinitialize(x, y, 0, 0, probeMode);
    this.guruWidth = width;
    this.guruHeight = height;
    if (probeMode) {
      currentStage = 0;
      subgoals.get(0).reinitialize(0, guruHeight - 100, 0, 0, true);
    }
  }
  
  @Override
  public void act(float delta) {
    super.act(delta);
    if (Math.round(ScienceEngine.getTime()) % 2 != 0) return;
    if (currentStage < 0 || currentStage == subgoals.size()) return;
    Subgoal subgoal = subgoals.get(currentStage);
    while (subgoal.hasSucceeded()) {
      currentStage++;
      stageBeginTime[currentStage] = ScienceEngine.getTime();
      science2DView.getGuru().done(true);
      if (currentStage == subgoals.size()) {
        break;
      }
      subgoal = subgoals.get(currentStage);
      subgoal.reinitialize(0, guruHeight - 50, 0, 0, true);
    }
  }

  /* (non-Javadoc)
   * @see com.mazalearn.scienceengine.guru.AbstractTutor#getHint()
   */
  @Override
  public String getHint() {
    if (currentStage < 0 || currentStage == subgoals.size()) return null;
    float timeElapsed = ScienceEngine.getTime() - stageBeginTime[currentStage];
    Subgoal subgoal = subgoals.get(currentStage);
    return subgoal.getGoal();
  }

  @Override
  public void checkProgress() {
    if (currentStage < 0 || currentStage == subgoals.size()) return;
    Subgoal subgoal = subgoals.get(currentStage);
    subgoal.checkProgress();
  }
  
  public void initialize(List<Subgoal> subgoals) {
    this.subgoals = subgoals;
    // End timeLimit of stage is begin timeLimit of stage i+1. So we need 1 extra
    this.stageBeginTime = new float[subgoals.size() + 1];
  }

  @Override
  public boolean hasSucceeded() {
    return currentStage == subgoals.size();
  }

  @Override
  public boolean hasFailed() {
    return false; // Allow learner to keep trying forever
  }

}