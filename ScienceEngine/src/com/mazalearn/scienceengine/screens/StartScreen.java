package com.mazalearn.scienceengine.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.experiments.MoleculeBox;
import com.mazalearn.scienceengine.experiments.StatesOfMatter;
import com.mazalearn.scienceengine.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.services.Profile;
import com.mazalearn.scienceengine.services.SoundManager.ScienceEngineSound;

public class StartScreen extends AbstractScreen {
  private Profile profile;

  private TextButton experimentButton;
  private Label creditsLabel;
  private ExperimentClickListener experimentClickListener;

  public StartScreen(ScienceEngine game) {
    super(game);
    // create the listeners
    experimentClickListener = new ExperimentClickListener(getSkin());
  }

  @Override
  public void show() {
    super.show();

    // start playing the menu music (the player might be returning from the
    // level screen)
    game.getMusicManager().play(ScienceEngineMusic.MENU);

    // retrieve the default table table
    Table table = super.getTable();
    table.defaults().spaceBottom(20);
    table.columnDefaults(0).padRight(20);
    table.columnDefaults(4).padLeft(10);
    table.add("Start Game").colspan(5);

    // retrieve the table's layout
    profile = game.getProfileManager().retrieveProfile();

    // create the experiment buttons
    table.row();
    table.add("Experiments");

    experimentButton = new TextButton("States of Matter", getSkin());
    experimentButton.setClickListener(experimentClickListener);
    table.add(experimentButton).fillX().padRight(10);

    // create the credits label
    table.row();
    creditsLabel = new Label(profile.getCreditsAsText(), getSkin());
    table.add("Credits");
    table.add(creditsLabel).left().colspan(4);

    // register the back button
    TextButton backButton = new TextButton("Back to main menu", getSkin());
    backButton.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {
        game.getSoundManager().play(ScienceEngineSound.CLICK);
        game.setScreen(new MenuScreen(game));
      }
    });
    table.row();
    table.add(backButton).size(250, 60).colspan(5);

    // set the select boxes' initial values
    updateValues();
  }

  private void updateValues() {
  }

  /**
   * Listener for all the level buttons.
   */
  private class ExperimentClickListener implements ClickListener {
    private Skin skin;
    ExperimentClickListener(Skin skin) {
      this.skin = skin;
    }
    @Override
    public void click(Actor actor, float x, float y) {
      game.getSoundManager().play(ScienceEngineSound.CLICK);
      Gdx.app.log(ScienceEngine.LOG, "Starting StatesOfMatter");
      Actor experiment = new StatesOfMatter(skin);
      game.setScreen(new ExperimentScreen(game, experiment, 
          "States of Matter"));
    }
  }

}
