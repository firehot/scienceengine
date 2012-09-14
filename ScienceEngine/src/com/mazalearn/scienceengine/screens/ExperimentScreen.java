package com.mazalearn.scienceengine.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
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
  private String experimentName;

  public ExperimentScreen(ScienceEngine scienceEngine, String experimentName) {
    super(scienceEngine);
    experimentController = createExperimentController(experimentName, 
        VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
    this.experimentName = experimentName;
  }

  @Override
  public void show() {
    super.show();
    Table table = super.getTable();
    table.add(experimentName).colspan(2);
    table.row();
    Profile profile = scienceEngine.getProfileManager().retrieveProfile();
    final IExperimentView experimentView = experimentController.getView();
    final int level = Math.max(profile.getCurrentLevelId(), 1);
    table.add("Current Level = " + level).colspan(2);
    table.row();
    
    for (int i = 0; i < 10; i++) {
      table.add("Level " + i).pad(10);
      table.add("description").pad(10);
      table.row();
    }
    
    TextButton experimentLevelButton = new TextButton("Go", getSkin());
    table.add(experimentLevelButton).fill().colspan(2);
    experimentLevelButton.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        setExperimentLevelScreen(experimentView, level);
      }
    });
  }

  private void setExperimentLevelScreen(IExperimentView experimentView,
      int level) {
    experimentView.getLevelManager().setLevel(level);
    experimentView.getLevelManager().load();
    if (ScienceEngine.DEV_MODE != DevMode.PRODUCTION) {
      levelEditor = new LevelEditor(experimentView.getLevelManager(),
          experimentController.getConfigurator(),
          (Stage) experimentView, experimentController.getModel(), this);
      levelEditor.enableEditor();
      this.setStage(levelEditor);
    } else {
      this.setStage((Stage) experimentView);      
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
}
