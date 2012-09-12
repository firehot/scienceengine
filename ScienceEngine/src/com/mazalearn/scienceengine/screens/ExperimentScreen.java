package com.mazalearn.scienceengine.screens;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.controller.IExperimentController;
import com.mazalearn.scienceengine.designer.LevelEditor;
import com.mazalearn.scienceengine.experiments.electromagnetism.ElectroMagnetismController;
import com.mazalearn.scienceengine.experiments.molecules.StatesOfMatterController;
import com.mazalearn.scienceengine.experiments.waves.WaveController;
import com.mazalearn.scienceengine.services.Profile;
import com.mazalearn.scienceengine.view.IExperimentView;

/**
 * IExperimentModel screen.
 */
public class ExperimentScreen extends AbstractScreen {

  private LevelEditor levelEditor;
  IExperimentController experimentController;

  public ExperimentScreen(ScienceEngine scienceEngine, String experimentName) {
    super(scienceEngine, null);
    experimentController = createExperimentController(experimentName, 
        VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
  }

  @Override
  public void show() {
    super.show();
    Profile profile = scienceEngine.getProfileManager().retrieveProfile();
    IExperimentView experimentView = experimentController.getView();
    int level = profile.getCurrentLevelId();
    if (level == 0) level = 1;
    experimentView.getLevelManager().setLevel(level);
    experimentView.getLevelManager().load();
    this.stage = (Stage) experimentView;
    if (ScienceEngine.DEV_MODE != DevMode.PRODUCTION) {
      levelEditor = new LevelEditor(experimentView.getLevelManager(), 
          stage, experimentController.getModel(), this);
      levelEditor.enableEditor();
      this.setStage(levelEditor);
    } else {
      this.setStage(stage);      
    }
  }

  private IExperimentController createExperimentController(
      String experimentName, int width, int height) {
    if (experimentName == StatesOfMatterController.NAME) {
      return new StatesOfMatterController(width, height, getSkin(), scienceEngine.getSoundManager());
    } else if (experimentName == WaveController.NAME) {
      return  new WaveController(width, height, getAtlas(), getSkin(), scienceEngine.getSoundManager());
    } else if (experimentName == ElectroMagnetismController.NAME) {
      return new ElectroMagnetismController(width, height, getSkin(), scienceEngine.getSoundManager());
    }
    return null;
  }
  
  @Override
  public boolean isGameScreen() {
    return true;
  }
}
