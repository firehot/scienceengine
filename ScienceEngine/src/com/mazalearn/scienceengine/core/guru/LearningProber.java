package com.mazalearn.scienceengine.core.guru;

import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.LevelLoader;
import com.mazalearn.scienceengine.core.model.IScience2DModel;

// outcome = function of parameter
// doubts on how parameter change affects magnitude of outcome
// Generate A, B as two parameter points.
// Is the outcome stronger at A or B?
public class LearningProber extends AbstractScience2DProber {
  
  private List<Stage> stages = Collections.emptyList();
  
  private IScience2DModel science2DModel;
  private Array<?> configs;

  private float[] stageBeginTime;
  private int currentStage;
    
  public LearningProber(IScience2DModel science2DModel, ProbeManager probeManager) {
    super(probeManager);
    this.science2DModel = science2DModel;
  }
  
  @Override
  public String getTitle() {
    return "Can you make number of revolutions of the motor +5 by configuring the magnetic field only?";
  }
  
  @Override
  public void reinitialize(float x, float y, float width, float height, boolean probeMode) {
    super.reinitialize(x,  y, 0, 0, probeMode);
    probeManager.setSize(width,  50);
    probeManager.setPosition(0, height - 50);
    LevelLoader.readConfigs(configs, science2DModel);
  }
  
  @Override
  public void activate(boolean activate) {
    if (activate) {
//      probeManager.setupProbeConfigs(Collections.<IModelConfig<?>> emptyList(), false);
      currentStage = 0;
      stageBeginTime[currentStage] = ScienceEngine.getTime();
    } 
    ScienceEngine.setProbeMode(activate);
    this.setVisible(activate);
  }

  @Override
  public String getHint() {
    float timeElapsed = ScienceEngine.getTime() - stageBeginTime[currentStage];
    // Provide a hint if current stage has not been completed and 
    // enough timeLimit has elapsed in this stage.
    Stage stage = stages.get(currentStage);
    if (stage.isStageCompleted(science2DModel)) {
      currentStage++;
      stageBeginTime[currentStage] = ScienceEngine.getTime();
    } else if (timeElapsed > stage.getTimeLimit()) {
      return stage.getHint();
    }
    
    return null;
  }

  public void setProbeConfig(Array<?> configs, List<Stage> stages) {
    this.configs = configs;
    this.stages = stages;
    // End timeLimit of stage is begin timeLimit of stage i+1. So we need 1 extra
    this.stageBeginTime = new float[stages.size() + 1];
  }
}