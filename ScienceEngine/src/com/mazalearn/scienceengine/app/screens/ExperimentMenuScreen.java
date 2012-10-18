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
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.experiments.electromagnetism.ElectroMagnetismController;
import com.mazalearn.scienceengine.experiments.molecules.StatesOfMatterController;
import com.mazalearn.scienceengine.experiments.waves.WaveController;

public class ExperimentMenuScreen extends AbstractScreen {
  private static final int THUMBNAIL_WIDTH = 200;
  private static final int THUMBNAIL_HEIGHT = 150;

  private Profile profile;

  public ExperimentMenuScreen(ScienceEngine scienceEngine) {
    super(scienceEngine);
    profile = ScienceEngine.getProfileManager().retrieveProfile();
  }

  @Override
  public void show() {
    super.show();
    if (!profile.getExperiment().equals("")) {
      gotoExperimentHome(profile.getExperiment());
      return;
    }
    setBackgroundColor(Color.DARK_GRAY);

    // start playing the menu music
    ScienceEngine.getMusicManager().play(ScienceEngineMusic.MENU);

    // retrieve the default table
    Table table = super.getTable();
    table.defaults().spaceBottom(20).fill().center();
    table.add(getMsg().getString("ScienceEngine.ScienceEngine")).colspan(10).spaceBottom(20); //$NON-NLS-1$
    table.row();

    // create the experiments Table
    table.add(createExperimentsSelector());    
    table.row();

    // register the back button
    TextButton backButton = new TextButton(getMsg().getString("ScienceEngine.BackToMain"), getSkin()); //$NON-NLS-1$
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
          gotoExperimentHome(experimentName);
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
  
  @Override
  protected void goBack() {
    ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
    scienceEngine.setScreen(new SplashScreen(scienceEngine));
  }
  
  private void gotoExperimentHome(final String experimentName) {
    Gdx.app.log(ScienceEngine.LOG, "Starting " + experimentName); //$NON-NLS-1$
    scienceEngine.setScreen(new ExperimentHomeScreen(scienceEngine, experimentName));
  }
}
