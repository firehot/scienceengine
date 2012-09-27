package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;

/**
 * Utility class for experimentModel action buttons using reflection.
 *
 */
public class ConfigOnOffButton implements IViewConfig {
  private final IModelConfig<Boolean> property;
  
  protected final TextButton toggleButton;
  
  public ConfigOnOffButton(final IModelConfig<Boolean> property, final Skin skin) {
    this.toggleButton = new TextButton(property.getName(), 
        skin.getStyle("toggle", TextButtonStyle.class));
    this.property = property;
    toggleButton.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        property.setValue(toggleButton.isChecked());
      }
    });
  }
  
  public Actor getActor() {
    return toggleButton;
  }

  @Override
  public void syncWithModel() {
    this.toggleButton.setChecked(property.getValue());
  }
  
  public boolean isAvailable() {
    return property.isAvailable();
  }
}