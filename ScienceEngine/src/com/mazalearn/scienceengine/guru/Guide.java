package com.mazalearn.scienceengine.guru;

import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.LevelLoader;
import com.mazalearn.scienceengine.core.model.IScience2DModel;

// outcome = function of parameter
// doubts on how parameter change affects magnitude of outcome
// Generate A, B as two parameter points.
// Is the outcome stronger at A or B?
public class Guide extends AbstractTutor {
  
  private List<Stage> stages = Collections.emptyList();
  
  private String title;
  private IScience2DModel science2DModel;
  private Array<?> configs;

  private float[] stageBeginTime;
  private int currentStage = -1;

  private String goal;

  private Guru guru;

  private float guruWidth;

  private float guruHeight;
    
  public Guide(IScience2DModel science2DModel, Guru guru) {
    this.science2DModel = science2DModel;
    this.guru = guru;
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
      guru.setSize(guruWidth,  50);
      guru.setPosition(0, guruHeight - 50);
      currentStage = 0;
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
    this.setPosition(x, y);
    this.setSize(0, 0);
    this.guruWidth = width;
    this.guruHeight = height;
    LevelLoader.readConfigs(configs, science2DModel);
  }
  
  @Override
  public void act(float delta) {
    super.act(delta);
    if (Math.round(ScienceEngine.getTime()) % 10 != 0) return;
    if (currentStage < 0 || currentStage == stages.size()) return;
    Stage stage = stages.get(currentStage);
    while (stage.isStageCompleted(science2DModel)) {
      currentStage++;
      stageBeginTime[currentStage] = ScienceEngine.getTime();
      if (currentStage == stages.size()) {
        guru.done(true);
        break;
      }
      stage = stages.get(currentStage);
    }
  }

  /* (non-Javadoc)
   * @see com.mazalearn.scienceengine.guru.AbstractTutor#getHint()
   */
  @Override
  public String getHint() {
    if (currentStage < 0 || currentStage == stages.size()) return null;
    float timeElapsed = ScienceEngine.getTime() - stageBeginTime[currentStage];
    Stage stage = stages.get(currentStage);
    if (timeElapsed > stage.getTimeLimit()) {
      return stage.getHint();
    }
    
    return null;
  }

  public void initialize(String goal, String title, Array<?> configs, 
      List<Stage> stages) {
    this.goal = goal;
    this.title = title;
    this.configs = configs;
    this.stages = stages;
    // End timeLimit of stage is begin timeLimit of stage i+1. So we need 1 extra
    this.stageBeginTime = new float[stages.size() + 1];
  }

  public int getSubsequentDeltaSuccessScore() {
    return 5;
  }

  @Override
  public int getDeltaSuccessScore() {
    return 0;
  }

  @Override
  public int getDeltaFailureScore() {
    return 0;
  }
}