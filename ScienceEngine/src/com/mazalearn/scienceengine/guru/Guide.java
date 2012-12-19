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
  
  private String title;
  private float[] stageBeginTime;
  private int currentStage = -1;

  private String goal;


  private float guruWidth;

  private float guruHeight;
    
  public Guide(IScience2DModel science2DModel, IScience2DView science2DView,
      int deltaSuccessScore, int deltaFailureScore) {
    super(science2DModel, science2DView, deltaSuccessScore, deltaFailureScore);
  }
  
  /* (non-Javadoc)
   * @see com.mazalearn.scienceengine.guru.AbstractTutor#getTitle()
   */
  @Override
  public String getTitle() {
    return title;
  }
  
  /* (non-Javadoc)
   * @see com.mazalearn.scienceengine.guru.AbstractTutor#activate(boolean)
   */
  @Override
  public void activate(boolean activate) {
    if (activate) {
      science2DView.getGuru().setSize(guruWidth,  50);
      science2DView.getGuru().setPosition(0, guruHeight - 50);
      currentStage = 0;
      subgoals.get(0).reinitialize(0, guruHeight - 50, 0, 0, true);
      stageBeginTime[currentStage] = ScienceEngine.getTime();
    } 
    ScienceEngine.setProbeMode(activate);
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
  }
  
  @Override
  public void act(float delta) {
    super.act(delta);
    if (Math.round(ScienceEngine.getTime()) % 2 != 0) return;
    if (currentStage < 0 || currentStage == subgoals.size()) return;
    Subgoal subgoal = subgoals.get(currentStage);
    while (subgoal.isCompleted()) {
      currentStage++;
      stageBeginTime[currentStage] = ScienceEngine.getTime();
      if (currentStage == subgoals.size()) {
        science2DView.getGuru().done(true);
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
    return subgoal.getHint();
  }

  @Override
  public void checkProgress() {
    if (currentStage < 0 || currentStage == subgoals.size()) return;
    Subgoal subgoal = subgoals.get(currentStage);
    subgoal.checkProgress();
  }
  
  public void initialize(String goal, String title, Array<?> components,
      Array<?> configs, List<Subgoal> subgoals) {
    super.initialize(components, configs);
    this.goal = goal;
    this.title = title;
    this.subgoals = subgoals;
    // End timeLimit of stage is begin timeLimit of stage i+1. So we need 1 extra
    this.stageBeginTime = new float[subgoals.size() + 1];
  }

  @Override
  public boolean isCompleted() {
    return currentStage == subgoals.size();
  }

}