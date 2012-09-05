package com.mazalearn.scienceengine.screens;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.esotericsoftware.tablelayout.Cell;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.controller.IExperimentController;
import com.mazalearn.scienceengine.designer.ScreenEditor;
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
  private Group view;

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
    view = (Group) experimentController.getView();
    table.add(view);
//        .width(GAME_VIEWPORT_WIDTH)
//        .height(GAME_VIEWPORT_HEIGHT);
    table.add(experimentController.getConfigurator()).width(100); // .height(960).fill();
    screenEditor = new ScreenEditor("data/" + experimentName + ".json", 
        (OrthographicCamera) stage.getCamera(), 
        (Group) experimentController.getView(), getBatch(), getFont());
    screenEditor.enable();
  }
  
  @Override
  public void dispose() {
    super.dispose();
  }
  
  @Override
  public void render(float delta) {
    experimentController.enable(!screenEditor.isEnabled());
    super.render(delta);
    screenEditor.render(view.x, view.y);
  }
  
  @Override
  public boolean isGameScreen() {
    return true;
  }
}
