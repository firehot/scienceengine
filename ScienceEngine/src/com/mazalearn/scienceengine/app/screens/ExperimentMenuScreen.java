package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.LevelManager;
import com.mazalearn.scienceengine.app.services.Messages;
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.experiments.electromagnetism.ElectroMagnetismController;
import com.mazalearn.scienceengine.experiments.molecules.StatesOfMatterController;
import com.mazalearn.scienceengine.experiments.waves.WaveController;

public class ExperimentMenuScreen extends AbstractScreen {
  private static final int THUMBNAIL_WIDTH = 200;
  private static final int THUMBNAIL_HEIGHT = 150;

  private Profile profile;

  public ExperimentMenuScreen(ScienceEngine scienceEngine) {
    super(scienceEngine);
  }

  @Override
  public void show() {
    super.show();
    setBackgroundColor(Color.DARK_GRAY);

    // start playing the menu music
    ScienceEngine.getMusicManager().play(ScienceEngineMusic.MENU);

    // retrieve the default table
    Table table = super.getTable();
    table.defaults().spaceBottom(20).fill().center();
    table.add(Messages.getString("ScienceEngine.ScienceEngine")).colspan(10).spaceBottom(20); //$NON-NLS-1$
    table.row();

    // retrieve the table's layout
    profile = ScienceEngine.getProfileManager().retrieveProfile();

    // create the experiments Table
    table.add(createExperimentsSelector());    
    table.row();

    // register the back button
    TextButton backButton = new TextButton(Messages.getString("ScienceEngine.BackToStart"), getSkin()); //$NON-NLS-1$
    backButton.addListener(new ClickListener() {
      public void clicked(InputEvent event, float x, float y) {
        goBack();
      }
    });
    table.row();
    table.add(backButton).colspan(10);
  }

  public Actor createExperimentsSelector() {
    Table table = new Table(getSkin());
    table.setName("Experiment Selector");
    ScrollPane flickScrollPane = new ScrollPane(table, getSkin());
    table.setFillParent(false);
    table.defaults().fill();
    final String[] experimentNames = 
        new String[] {StatesOfMatterController.NAME, 
                      WaveController.NAME, 
                      ElectroMagnetismController.NAME};
    for (final String experimentName: experimentNames) {
      Image experimentThumb = 
          new Image(LevelManager.getThumbnail(experimentName, 1));
      experimentThumb.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
          Gdx.app.log(ScienceEngine.LOG, "Starting " + experimentName); //$NON-NLS-1$
          IScience2DController science2DController = 
              createExperimentController(experimentName, 
              VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
          scienceEngine.setScreen(new ExperimentHomeScreen(scienceEngine, science2DController));
        }
      });
      Table levelTable = new Table(getSkin());
      levelTable.setName("Level");
      levelTable.add(experimentName);
      levelTable.row();
      levelTable.add(experimentThumb).width(THUMBNAIL_WIDTH).height(THUMBNAIL_HEIGHT);
      table.add(levelTable).pad(5);
    }
    return flickScrollPane;
  }
  
  private IScience2DController createExperimentController(
      String experimentName, int width, int height) {
    if (experimentName == StatesOfMatterController.NAME) {
      return new StatesOfMatterController(width, height, getSkin());
    } else if (experimentName == WaveController.NAME) {
      return  new WaveController(width, height, getAtlas(), getSkin());
    } else if (experimentName == ElectroMagnetismController.NAME) {
      return new ElectroMagnetismController(width, height, getSkin());
    }
    return null;
  }

  @Override
  protected void goBack() {
    ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
    scienceEngine.setScreen(new StartScreen(scienceEngine));
  }
  
}
