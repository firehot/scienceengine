package com.mazalearn.scienceengine.screens;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.controller.IExperimentController;
import com.mazalearn.scienceengine.designer.ScreenEditor;
import com.mazalearn.scienceengine.experiments.electromagnetism.ElectroMagnetismController;
import com.mazalearn.scienceengine.experiments.molecules.StatesOfMatterController;
import com.mazalearn.scienceengine.experiments.waves.WaveController;
import com.mazalearn.scienceengine.services.Profile;
import com.mazalearn.scienceengine.view.AbstractExperimentView;

/**
 * IExperimentModel screen.
 */
public class ExperimentScreen extends AbstractScreen {

  final String experimentName;
  private ScreenEditor screenEditor;
  IExperimentController experimentController;

  public ExperimentScreen(ScienceEngine scienceEngine, String experimentName) {
    super(scienceEngine, null);
    this.experimentName = experimentName;
    experimentController = createExperimentController(experimentName, 
        VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
    this.setStage((AbstractExperimentView) experimentController.getView());
  }

  @Override
  public void show() {
    super.show();
    Profile profile = scienceEngine.getProfileManager().retrieveProfile();
    screenEditor = new ScreenEditor(experimentName, profile.getCurrentLevelId(), 
        (Stage) experimentController.getView(), experimentController.getModel(),
        getFont(), getSkin());
    screenEditor.enable();
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
  public void dispose() {
    super.dispose();
  }
  
  @Override
  public void render(float delta) {
    experimentController.enable(!screenEditor.isEnabled() && 
        experimentController.getModel().isEnabled());
    super.render(delta);
    screenEditor.draw();
  }
  
  @Override
  public boolean isGameScreen() {
    return true;
  }
}
