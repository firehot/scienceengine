package com.mazalearn.scienceengine.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.services.Profile;
import com.mazalearn.scienceengine.services.SoundManager.ScienceEngineSound;

/**
 * A simple high scores screen.
 */
public class HighScoresScreen extends AbstractScreen {
  public HighScoresScreen(ScienceEngine game) {
    super(game);
  }

  @Override
  public void show() {
    super.show();
    Profile profile = game.getProfileManager().retrieveProfile();

    // retrieve the default table actor
    Table table = super.getTable();
    table.defaults().spaceBottom(30);
    table.add("High scores").colspan(2);

    // episode 1 high-score
    String level1Highscore = String.valueOf(profile.getHighScore(0));
    Label episode1HighScore = new Label(level1Highscore, getSkin());
    table.row();
    table.add("Episode 1");
    table.add(episode1HighScore);

    String level2Highscore = String.valueOf(profile.getHighScore(1));
    Label episode2HighScore = new Label(level2Highscore, getSkin());
    table.row();
    table.add("Episode 2").center();
    table.add(episode2HighScore);

    String level3Highscore = String.valueOf(profile.getHighScore(2));
    Label episode3HighScore = new Label(level3Highscore, getSkin());
    table.row();
    table.add("Episode 3");
    table.add(episode3HighScore);

    // register the back button
    TextButton backButton = new TextButton("Back to main menu", getSkin());
    backButton.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        game.getSoundManager().play(ScienceEngineSound.CLICK);
        game.setScreen(new MenuScreen(game));
      }
    });
    table.row();
    table.add(backButton).size(250, 60).colspan(2);
  }
}
