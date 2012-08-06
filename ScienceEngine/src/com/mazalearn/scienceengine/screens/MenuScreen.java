package com.mazalearn.scienceengine.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.services.SoundManager.ScienceEngineSound;

public class MenuScreen extends AbstractScreen {
  public MenuScreen(ScienceEngine game) {
    super(game);
  }

  @Override
  public void show() {
    super.show();

    // retrieve the default table actor
    Table table = super.getTable();
    table.add("Welcome to Science Engine!").spaceBottom(50);
    table.row();

    // register the button "start game"
    TextButton startGameButton = new TextButton("Start", getSkin());
    startGameButton.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        game.getSoundManager().play(ScienceEngineSound.CLICK);
        game.setScreen(new StartScreen(game));
      }
    });
    table.add(startGameButton).size(300, 60).uniform().spaceBottom(10);
    table.row();

    // register the button "options"
    TextButton optionsButton = new TextButton("Options", getSkin());
    optionsButton.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        game.getSoundManager().play(ScienceEngineSound.CLICK);
        game.setScreen(new OptionsScreen(game));
      }
    });
    table.add(optionsButton).uniform().fill().spaceBottom(10);
    table.row();
/*
    // register the button "high scores"
    TextButton highScoresButton = new TextButton("High Scores", getSkin());
    highScoresButton.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        game.getSoundManager().play(ScienceEngineSound.CLICK);
        game.setScreen(new HighScoresScreen(game));
      }
    });
    table.add(highScoresButton).uniform().fill();
  */
  }
}
