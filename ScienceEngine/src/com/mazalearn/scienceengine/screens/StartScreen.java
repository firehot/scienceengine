package com.mazalearn.scienceengine.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.services.Profile;
import com.mazalearn.scienceengine.services.SoundManager.ScienceEngineSound;

public class StartScreen extends AbstractScreen {
  private Profile profile;

  private TextButton statesOfMatterButton;
  private Label creditsLabel;
  private LevelClickListener levelClickListener;

  public StartScreen(ScienceEngine game) {
    super(game);
    // create the listeners
    levelClickListener = new LevelClickListener();
  }

  @Override
  public void show() {
    super.show();

    // start playing the menu music (the player might be returning from the
    // level screen)
    game.getMusicManager().play(ScienceEngineMusic.MENU);

    // retrieve the default table actor
    Table table = super.getTable();
    table.defaults().spaceBottom(20);
    table.columnDefaults(0).padRight(20);
    table.columnDefaults(4).padLeft(10);
    table.add("Start Game").colspan(5);

    // retrieve the table's layout
    profile = game.getProfileManager().retrieveProfile();

    // create the level buttons
    table.row();
    table.add("Episodes");

    statesOfMatterButton = new TextButton( "States of Matter", getSkin() );
    statesOfMatterButton.setClickListener(levelClickListener);
    table.add( statesOfMatterButton ).fillX().padRight( 10 );

    // create the credits label
    creditsLabel = new Label(profile.getCreditsAsText(), getSkin());
    table.row();
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
  private class LevelClickListener implements ClickListener {
    @Override
    public void click(Actor actor, float x, float y) {
      game.getSoundManager().play(ScienceEngineSound.CLICK);
      Gdx.app.log(ScienceEngine.LOG, "Starting StatesOfMatter");
      game.setScreen(new StatesOfMatterScreen(game));
    }
  }

}
