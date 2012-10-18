package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.app.services.LevelManager;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.ProfileManager;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.view.IScience2DStage;

/**
 * Experiment screen corresponding to one level of the experiment.
 */
public class ExperimentScreen extends AbstractScreen {

  private IScience2DController science2DController;
  private Profile profile;
  private String experimentName;

  public ExperimentScreen(ScienceEngine scienceEngine, 
      int level, String experimentName) {
    super(scienceEngine, null);
    this.experimentName = experimentName;
    this.science2DController = 
        scienceEngine.createExperimentController(experimentName, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
    IScience2DStage science2DStage = science2DController.getView();
    ProfileManager profileManager = ScienceEngine.getProfileManager();
    profile = profileManager.retrieveProfile();
    profile.setCurrentLevel(level);
    LevelManager levelManager = science2DStage.getLevelManager();
    levelManager.setLevel(level);
    levelManager.load();
    if (ScienceEngine.DEV_MODE == DevMode.DESIGN) {
      Stage levelEditor = 
          ScienceEngine.getPlatformAdapter().createLevelEditor(science2DController, this);
      this.setStage(levelEditor);
    } else {
      this.setStage((Stage) science2DStage);      
    }
    stage.addListener(new InputListener() {
      @Override
      public boolean keyDown(InputEvent event, int keycode) {
        if (keycode == Keys.BACK) {
          goBack();
          return true;
        }
        return super.keyDown(event, keycode);
      }      
    });
  }

  @Override
  protected void goBack() {
    ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
    scienceEngine.setScreen(new ExperimentHomeScreen(scienceEngine, experimentName));
    profile.setCurrentLevel(0);
  }
  
}
