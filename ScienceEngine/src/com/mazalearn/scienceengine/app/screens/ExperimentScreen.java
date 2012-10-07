package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.app.services.LevelManager;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.view.IScience2DStage;
import com.mazalearn.scienceengine.designer.LevelEditor;

/**
 * Experiment screen corresponding to one level of the experiment.
 */
public class ExperimentScreen extends AbstractScreen {

  private IScience2DController science2DController;

  public ExperimentScreen(ScienceEngine scienceEngine, 
      LevelManager levelManager, int level, IScience2DController science2DController) {
    super(scienceEngine, null);
    this.science2DController = science2DController;
    IScience2DStage science2DStage = science2DController.getView();
    levelManager.setLevel(level);
    levelManager.load();
    if (ScienceEngine.DEV_MODE == DevMode.DESIGN) {
      LevelEditor levelEditor = new LevelEditor(levelManager,
          science2DController.getControlPanel(),
          (Stage) science2DStage, science2DController.getModel(), this);
      levelEditor.enableEditor();
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
    scienceEngine.setScreen(new ExperimentHomeScreen(scienceEngine, science2DController));
  }
  
}
