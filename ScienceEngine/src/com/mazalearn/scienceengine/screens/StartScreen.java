package com.mazalearn.scienceengine.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.SelectionListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.controller.IExperimentController;
import com.mazalearn.scienceengine.experiments.electromagnetism.ElectroMagnetismController;
import com.mazalearn.scienceengine.experiments.molecules.StatesOfMatterController;
import com.mazalearn.scienceengine.experiments.waves.WaveController;
import com.mazalearn.scienceengine.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.services.Profile;
import com.mazalearn.scienceengine.services.SoundManager.ScienceEngineSound;

public class StartScreen extends AbstractScreen {
  private Profile profile;

  private SelectionListener experimentSelectListener;

  public StartScreen(ScienceEngine scienceEngine) {
    super(scienceEngine);
    // create the listeners
    experimentSelectListener = new ExperimentSelectListener();
  }

  @Override
  public void show() {
    super.show();

    // start playing the menu music (the player might be returning from the
    // level screen)
    scienceEngine.getMusicManager().play(ScienceEngineMusic.MENU);

    // retrieve the default table table
    Table table = super.getTable();
    table.defaults().spaceBottom(20);
    table.columnDefaults(0).padRight(20);
    table.columnDefaults(4).padLeft(10);
    table.add("Science Engine").colspan(2);
    table.row();

    // retrieve the table's layout
    profile = scienceEngine.getProfileManager().retrieveProfile();

    // Add checkbox if designer mode is to be enabled
    CheckBox designerMode = new CheckBox("Designer Mode", scienceEngine.getSkin());
    designerMode.setChecked(ScienceEngine.DEV_MODE != DevMode.PRODUCTION);
    designerMode.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        if (ScienceEngine.DEV_MODE == DevMode.PRODUCTION) {
          ScienceEngine.DEV_MODE = DevMode.DEBUG;
        } else {
          ScienceEngine.DEV_MODE = DevMode.PRODUCTION;
        }
      }   
    });
    table.add(designerMode).colspan(2);
    table.row();
    
    // create the experiment select box
    table.row();
    table.add("Experiments");

    String[] experiments = 
        new String[] {StatesOfMatterController.NAME, 
                      WaveController.NAME, 
                      ElectroMagnetismController.NAME};
    SelectBox experimentSelectBox = new SelectBox(experiments, scienceEngine.getSkin());
    table.add(experimentSelectBox);
    experimentSelectBox.setSelectionListener(experimentSelectListener);
    table.row();

    // register the back button
    TextButton backButton = new TextButton("Back to main menu", scienceEngine.getSkin());
    backButton.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {
        scienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        scienceEngine.setScreen(new MenuScreen(scienceEngine));
      }
    });
    table.row();
    table.add(backButton).size(250, 60).colspan(2);
  }
  
  /**
   * Listener for experimentModel click button.
   */
  private class ExperimentSelectListener implements SelectionListener {
    @Override
    public void selected(Actor actor, int index, String experimentName) {
      scienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
      Gdx.app.log(ScienceEngine.LOG, "Starting " + experimentName);
      IExperimentController experimentController = 
          createExperimentController(experimentName, 
          VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
      scienceEngine.setScreen(new ExperimentHomeScreen(scienceEngine, experimentController));
    }
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
