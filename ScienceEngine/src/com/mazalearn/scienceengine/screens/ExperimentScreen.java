package com.mazalearn.scienceengine.screens;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.controller.IExperimentController;
import com.mazalearn.scienceengine.devtools.ScreenEditor;
import com.mazalearn.scienceengine.experiments.electromagnetism.ElectroMagnetismController;
import com.mazalearn.scienceengine.experiments.molecules.StatesOfMatterController;
import com.mazalearn.scienceengine.experiments.waves.WaveController;

/**
 * IExperimentModel screen.
 */
public class ExperimentScreen extends AbstractScreen {

  final String experimentName;
  private ScreenEditor screenEditor;
  IExperimentController experimentController;

  public ExperimentScreen(ScienceEngine game, String experimentName) {
    super(game);
    this.experimentName = experimentName;
  }

  @Override
  public void show() {
    super.show();
    
    // retrieve the default table actor
    Table table = super.getTable();
    table.add(experimentName).spaceBottom(10);
    table.row();

    // Add States of Matter experimentModel to table
    int width = (int) stage.width() - 100;
    int height = (int) stage.height();
    if (experimentName == "States of Matter") {
      experimentController = new StatesOfMatterController(width, height, getSkin());
    } else if (experimentName == "Wave Motion") {
      experimentController = new WaveController(width, height, getAtlas(), getSkin());
    } else if (experimentName == "Electromagnetism") {
      experimentController = new ElectroMagnetismController(width, height, getSkin());
    }
    table.add((Group) experimentController.getView());
//        .width(GAME_VIEWPORT_WIDTH)
//        .height(GAME_VIEWPORT_HEIGHT);
    table.add(experimentController.getConfigurator()).width(100); // .height(960).fill();
    screenEditor = new ScreenEditor("data/" + experimentName + ".json", 
        (OrthographicCamera) stage.getCamera(), 
        experimentController.getComponents(), getBatch(), getFont());
    screenEditor.enable();
  }
  
  @Override
  public void dispose() {
    super.dispose();
  }
  
  @Override
  public void render(float delta) {
    super.render(delta);
    experimentController.enable(screenEditor.isEnabled());
    screenEditor.render();
  }
  
  @Override
  public boolean isGameScreen() {
    return true;
  }
}
