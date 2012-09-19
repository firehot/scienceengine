package com.mazalearn.scienceengine.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.controller.IExperimentController;
import com.mazalearn.scienceengine.experiments.electromagnetism.ElectroMagnetismController;
import com.mazalearn.scienceengine.experiments.molecules.StatesOfMatterController;
import com.mazalearn.scienceengine.experiments.waves.WaveController;
import com.mazalearn.scienceengine.services.LevelManager;
import com.mazalearn.scienceengine.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.services.Profile;
import com.mazalearn.scienceengine.services.SoundManager.ScienceEngineSound;

public class ExperimentMenuScreen extends AbstractScreen {
  private static final int NUM_COLUMNS = 3;

  private Profile profile;

  public ExperimentMenuScreen(ScienceEngine scienceEngine) {
    super(scienceEngine);
  }

  @Override
  public void show() {
    super.show();
    setBackgroundColor(Color.DARK_GRAY);

    // start playing the menu music
    scienceEngine.getMusicManager().play(ScienceEngineMusic.MENU);

    // retrieve the default table
    Table table = super.getTable();
    table.defaults().spaceBottom(20).fill().center();
    table.add("Science Engine").colspan(NUM_COLUMNS).spaceBottom(20);
    table.row();

    // retrieve the table's layout
    profile = scienceEngine.getProfileManager().retrieveProfile();

    // create the experiments Table
    final String[] experimentNames = 
        new String[] {StatesOfMatterController.NAME, 
                      WaveController.NAME, 
                      ElectroMagnetismController.NAME};
    int count = 0;
    for (final String experimentName: experimentNames) {
      count++;
      Image experimentThumb = 
          new Image(LevelManager.getThumbnail(experimentName, 1));
      experimentThumb.setClickListener(new ClickListener() {
        @Override
        public void click(Actor actor, float x, float y) {
          scienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
          Gdx.app.log(ScienceEngine.LOG, "Starting " + experimentName);
          IExperimentController experimentController = 
              createExperimentController(experimentName, 
              VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
          scienceEngine.setScreen(new ExperimentHomeScreen(scienceEngine, experimentController));
        }
      });
      Table levelTable = table.newTable();
      levelTable.add(experimentName);
      levelTable.row();
      levelTable.add(experimentThumb);
      table.add(levelTable).pad(5);
      if (count % NUM_COLUMNS == 0) {
        table.row();
      }
    }    
    table.row();

    // register the back button
    TextButton backButton = new TextButton("Back to Start", getSkin());
    backButton.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {
        scienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        scienceEngine.setScreen(new StartScreen(scienceEngine));
      }
    });
    table.row();
    table.add(backButton).colspan(NUM_COLUMNS);
  }
  
  private IExperimentController createExperimentController(
      String experimentName, int width, int height) {
    if (experimentName == StatesOfMatterController.NAME) {
      return new StatesOfMatterController(width, height, scienceEngine.getSkin(), scienceEngine.getSoundManager());
    } else if (experimentName == WaveController.NAME) {
      return  new WaveController(width, height, getAtlas(), scienceEngine.getSkin(), scienceEngine.getSoundManager());
    } else if (experimentName == ElectroMagnetismController.NAME) {
      return new ElectroMagnetismController(width, height, scienceEngine.getSkin(), scienceEngine.getSoundManager());
    }
    return null;
  }
  
}
