package com.mazalearn.scienceengine.guru;

import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.IScience2DController;

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
    
  public Guide(IScience2DController science2DController,
      String goal, Array<?> components, Array<?> configs, int deltaSuccessScore, int deltaFailureScore) {
    super(science2DController, goal, components, configs, deltaSuccessScore, deltaFailureScore);
  }
  
  /* (non-Javadoc)
   * @see com.mazalearn.scienceengine.guru.AbstractTutor#activate(boolean)
   */
  @Override
  public void activate(boolean activate) {
    if (activate) {
      science2DController.getGuru().setSize(guruWidth,  50);
      science2DController.getGuru().setPosition(0, guruHeight - 50);
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
      activateStage(0);
    }
  }
  
  @Override
  public void act(float delta) {
    super.act(delta);
    if (Math.round(ScienceEngine.getTime()) % 2 != 0) return;
    if (currentStage < 0 || currentStage == subgoals.size()) return;
    Subgoal subgoal = subgoals.get(currentStage);
    while (subgoal.hasSucceeded()) {
      subgoal.activate(false);
      currentStage++;
      stageBeginTime[currentStage] = ScienceEngine.getTime();
      science2DController.getGuru().done(true);
      if (currentStage == subgoals.size()) {
        break;
      }
      subgoal = activateStage(currentStage);
    }
  }

  private Subgoal activateStage(int currentStage) {
    this.currentStage = currentStage;
    Subgoal subgoal = subgoals.get(currentStage);
    subgoal.reinitialize(0, guruHeight - 50, 0, 0, true);
    subgoal.activate(true);
    return subgoal;
  }

  /* (non-Javadoc)
   * @see com.mazalearn.scienceengine.guru.AbstractTutor#getHint()
   */
  @Override
  public String getHint() {
    if (currentStage < 0 || currentStage == subgoals.size()) return null;
    // float timeElapsed = ScienceEngine.getTime() - stageBeginTime[currentStage];
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
    for (Subgoal subgoal: subgoals) {
      addActor(subgoal);
      subgoal.activate(false);
    }
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