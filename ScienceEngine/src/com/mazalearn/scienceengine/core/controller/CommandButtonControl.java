package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

/**
 * Utility class for science2DModel action buttons using reflection.
 *
 */
public class CommandButtonControl implements IControl {
  @SuppressWarnings("rawtypes")
  private final IModelConfig command;
  
  protected final TextButton textButton;
  
  @SuppressWarnings("rawtypes")
  public CommandButtonControl(final IModelConfig command, final Skin skin) {
    this.textButton = new TextButton(command.getName(), skin);
    this.command = command;
    textButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
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