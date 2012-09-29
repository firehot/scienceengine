package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.app.services.LevelManager;
import com.mazalearn.scienceengine.core.controller.IExperimentController;
import com.mazalearn.scienceengine.core.view.IScience2DStage;
import com.mazalearn.scienceengine.designer.LevelEditor;

/**
 * Experiment screen corresponding to one level of the experiment.
 */
public class ExperimentScreen extends AbstractScreen {

  public ExperimentScreen(ScienceEngine scienceEngine, 
      LevelManager levelManager, int level, IExperimentController experimentController) {
    super(scienceEngine, null);
    IScience2DStage science2DStage = experimentController.getView();
    levelManager.setLevel(level);
    levelManager.load();
    if (ScienceEngine.DEV_MODE == DevMode.DESIGN) {
      LevelEditor levelEditor = new LevelEditor(levelManager,
          experimentController.getControlPanel(),
          (Stage) science2DStage, experimentController.getModel(), this);
      levelEditor.enableEditor();
      this.setStage(levelEditor);
    } else {
      this.setStage((Stage) science2DStage);      
    }
  }
}
