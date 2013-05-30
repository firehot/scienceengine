package com.mazalearn.scienceengine.core.view;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;

public abstract class CommandClickListener extends ClickListener {
  
  @Override
  public void clicked (InputEvent event, float x, float y) {
    ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
    doCommand();
  }
  
  abstract public void doCommand();
}
