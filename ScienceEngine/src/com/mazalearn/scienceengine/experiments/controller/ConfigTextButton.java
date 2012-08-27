package com.mazalearn.scienceengine.experiments.controller;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

/**
 * Utility class for iExperimentModel action buttons using reflection.
 *
 */
public class ConfigTextButton extends TextButton implements IViewConfig {
  @SuppressWarnings("rawtypes")
  private final IModelConfig command;
  @SuppressWarnings("rawtypes")
  public ConfigTextButton(final IModelConfig command, final Skin skin) {
    super(command.getName(), skin);
    this.command = command;
    this.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        command.doCommand();
      }
    });
  }

  @Override
  public void syncWithModel() {
  }
  
  public boolean isAvailable() {
    return command.isAvailable();
  }
}