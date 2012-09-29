package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.Messages;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;

public class StartScreen extends AbstractScreen {
  public StartScreen(ScienceEngine scienceEngine) {
    super(scienceEngine);
  }

  @Override
  public void show() {
    super.show();

    // retrieve the default table actor
    Table table = super.getTable();
    table.add(Messages.getString("ScienceEngine.ScienceEngine")).spaceBottom(50); //$NON-NLS-1$
    table.row();

    registerButton(table, Messages.getString("ScienceEngine.Experiments"), new ExperimentMenuScreen(scienceEngine)); //$NON-NLS-1$
    registerButton(table, Messages.getString("ScienceEngine.Options"), new OptionsScreen(scienceEngine)); //$NON-NLS-1$
    TextButton startGameButton = new TextButton(Messages.getString("ScienceEngine.Exit"), scienceEngine.getSkin()); //$NON-NLS-1$
    startGameButton.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
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
