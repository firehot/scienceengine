package com.mazalearn.scienceengine.screens;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.controller.IExperimentController;
import com.mazalearn.scienceengine.designer.LevelEditor;
import com.mazalearn.scienceengine.services.LevelManager;
import com.mazalearn.scienceengine.view.IExperimentView;

/**
 * Experiment Level screen corresponding to one level of the experiment.
 */
public class ExperimentLevelScreen extends AbstractScreen {

  public ExperimentLevelScreen(ScienceEngine scienceEngine, 
      LevelManager levelManager, IExperimentController experimentController) {
    super(scienceEngine, null);
    IExperimentView experimentView = experimentController.getView();
    levelManager.load();
    if (ScienceEngine.DEV_MODE != DevMode.PRODUCTION) {
      LevelEditor levelEditor = new LevelEditor(levelManager,
          experimentController.getConfigurator(),
          (Stage) experimentView, experimentController.getModel(), this);
      levelEditor.enableEditor();
      this.setStage(levelEditor);
    } else {
      this.setStage((Stage) experimentView);      
    }
  }
}
