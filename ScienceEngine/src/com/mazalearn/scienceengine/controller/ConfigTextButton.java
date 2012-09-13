package com.mazalearn.scienceengine.controller;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

/**
 * Utility class for experimentModel action buttons using reflection.
 *
 */
public class ConfigTextButton implements IViewConfig {
  @SuppressWarnings("rawtypes")
  private final IModelConfig command;
  
  protected final TextButton textButton;
  
  @SuppressWarnings("rawtypes")
  public ConfigTextButton(final IModelConfig command, final Skin skin) {
    this.textButton = new TextButton(command.getName(), skin);
    this.command = command;
    textButton.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        command.doCommand();
      }
    });
  }
  
  public Actor getActor() {
    return textButton;
  }

  @Override
  public void syncWithModel() {
  }
  
  public boolean isAvailable() {
    return command.isAvailable();
  }
}