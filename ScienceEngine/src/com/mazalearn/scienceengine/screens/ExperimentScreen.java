package com.mazalearn.scienceengine.screens;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.controller.IExperimentController;
import com.mazalearn.scienceengine.designer.LevelEditor;
import com.mazalearn.scienceengine.experiments.electromagnetism.ElectroMagnetismController;
import com.mazalearn.scienceengine.experiments.molecules.StatesOfMatterController;
import com.mazalearn.scienceengine.experiments.waves.WaveController;
import com.mazalearn.scienceengine.services.LevelManager;
import com.mazalearn.scienceengine.services.Profile;

/**
 * IExperimentModel screen.
 */
public class ExperimentScreen extends AbstractScreen {

  final private String experimentName;
  private LevelEditor levelEditor;
  IExperimentController experimentController;

  public ExperimentScreen(ScienceEngine scienceEngine, String experimentName) {
    super(scienceEngine, null);
    this.experimentName = experimentName;
    experimentController = createExperimentController(experimentName, 
        VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
  }

  @Override
  public void show() {
    super.show();
    Profile profile = scienceEngine.getProfileManager().retrieveProfile();
    Stage stage = (Stage) experimentController.getView();
    int level = profile.getCurrentLevelId();
    if (level == 0) level = 1;
    LevelManager levelManager = new LevelManager(experimentName, level, stage, 
        experimentController.getModel().getConfigs());
    levelManager.loadLevel();
    if (ScienceEngine.DEV_MODE != DevMode.PRODUCTION) {
      levelEditor = new LevelEditor(levelManager, 
          stage, experimentController.getModel(), this);
      levelEditor.enableEditor();
      this.setStage(levelEditor);
    } else {
      this.setStage(stage);      
    }
  }

  private IExperimentController createExperimentController(
      String experimentName, int width, int height) {
    if (experimentName == "States of Matter") {
      return new StatesOfMatterController(width, height, getSkin());
    } else if (experimentName == "Wave Motion") {
      return  new WaveController(width, height, getAtlas(), getSkin());
    } else if (experimentName == "Electromagnetism") {
      return new ElectroMagnetismController(width, height, getSkin());
    }
    return null;
  }
  
  @Override
  public boolean isGameScreen() {
    return true;
  }
}
