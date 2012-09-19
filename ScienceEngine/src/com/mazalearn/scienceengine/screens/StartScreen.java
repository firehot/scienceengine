package com.mazalearn.scienceengine.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.services.SoundManager.ScienceEngineSound;

public class StartScreen extends AbstractScreen {
  public StartScreen(ScienceEngine scienceEngine) {
    super(scienceEngine);
  }

  @Override
  public void show() {
    super.show();

    // retrieve the default table actor
    Table table = super.getTable();
    table.add("Science Engine!").spaceBottom(50);
    table.row();

    registerButton(table, "Experiments", new ExperimentMenuScreen(scienceEngine));
    registerButton(table, "Options", new OptionsScreen(scienceEngine));
    TextButton startGameButton = new TextButton("Exit", scienceEngine.getSkin());
    startGameButton.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        scienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        Gdx.app.exit();
      }
    });
    table.add(startGameButton).size(300, 60).uniform().spaceBottom(10);
    table.row();
  }

  protected void registerButton(Table table, String name, final Screen screen) {
    TextButton startGameButton = new TextButton(name, scienceEngine.getSkin());
    startGameButton.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        scienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        scienceEngine.setScreen(screen);
      }
    });
    table.add(startGameButton).size(300, 60).uniform().spaceBottom(10);
    table.row();
  }
}
