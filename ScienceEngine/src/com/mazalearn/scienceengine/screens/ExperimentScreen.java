package com.mazalearn.scienceengine.screens;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.controller.IExperimentController;
import com.mazalearn.scienceengine.designer.ScreenEditor;
import com.mazalearn.scienceengine.experiments.electromagnetism.ElectroMagnetismController;
import com.mazalearn.scienceengine.experiments.molecules.StatesOfMatterController;
import com.mazalearn.scienceengine.experiments.waves.WaveController;
import com.mazalearn.scienceengine.view.AbstractExperimentView;

/**
 * IExperimentModel screen.
 */
public class ExperimentScreen extends AbstractScreen {

  final String experimentName;
  private ScreenEditor screenEditor;
  IExperimentController experimentController;

  public ExperimentScreen(ScienceEngine game, String experimentName) {
    super(game, null);
    this.experimentName = experimentName;
    experimentController = createExperimentController(experimentName, 
        GAME_VIEWPORT_WIDTH, GAME_VIEWPORT_HEIGHT);
    this.setStage((AbstractExperimentView) experimentController.getView());
  }

  @Override
  public void show() {
    super.show();
    
    // retrieve the default table actor
    //Table table = super.getTable();
    //table.add(experimentName).spaceBottom(10);
    //table.row();

    //table.add(experimentController.getView());
//        .width(GAME_VIEWPORT_WIDTH)
//        .height(GAME_VIEWPORT_HEIGHT);
    //table.add(experimentController.getConfigurator()).width(100); // .height(960).fill();
    screenEditor = new ScreenEditor(experimentName, 
        (Stage) experimentController.getView(), 
        getFont());
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
