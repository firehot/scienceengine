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
public class Guide extends Group {
  
  private List<Stage> stages = Collections.emptyList();
  
  private String title;
  private IScience2DModel science2DModel;
  private Array<?> configs;

  private float[] stageBeginTime;
  private int currentStage = -1;

  private String goal;

  private List<AbstractScience2DProber> probers;

  private Guru guru;

  private float guruWidth;

  private float guruHeight;
    
  public Guide(IScience2DModel science2DModel, Guru guru) {
    this.science2DModel = science2DModel;
    this.guru = guru;
  }
  
  public String getTitle() {
    return title;
  }
  
  public void activate(boolean activate) {
    if (activate) {
      guru.setSize(guruWidth,  50); // ??? TODO: how to handle wrt probes ???
      guru.setPosition(0, guruHeight - 50);
      currentStage = 0;
      stageBeginTime[currentStage] = ScienceEngine.getTime();
    } 
    ScienceEngine.setProbeMode(activate);
    this.setVisible(activate);
  }

  public void reinitialize(float x, float y, float width, float height) {
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
        guru.guideDone();
        break;
      }
      stage = stages.get(currentStage);
    }
  }

  public String getHint() {
    if (currentStage < 0 || currentStage == stages.size()) return null;
    float timeElapsed = ScienceEngine.getTime() - stageBeginTime[currentStage];
    Stage stage = stages.get(currentStage);
    if (timeElapsed > stage.getTimeLimit()) {
      return stage.getHint();
    }
    
    return null;
  }

  public List<AbstractScience2DProber> getProbers() {
    return probers;
  }
  
  public void setProbeConfig(String goal, String title, Array<?> configs, 
      List<Stage> stages, List<AbstractScience2DProber> probers) {
    this.goal = goal;
    this.title = title;
    this.configs = configs;
    this.stages = stages;
    this.probers = probers;
    // End timeLimit of stage is begin timeLimit of stage i+1. So we need 1 extra
    this.stageBeginTime = new float[stages.size() + 1];
  }

  public int getSubsequentDeltaSuccessScore() {
    return 5;
  }
}