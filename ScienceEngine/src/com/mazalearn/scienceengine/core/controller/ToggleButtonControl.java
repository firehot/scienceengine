package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.view.IScience2DView;

/**
 * Utility class for science2DModel action buttons using reflection.
 *
 */
public class ToggleButtonControl implements IControl {
  private final IModelConfig<Boolean> property;
  
  protected final TextButton toggleButton;
  
  public ToggleButtonControl(final IModelConfig<Boolean> property, final Skin skin) {
    this.toggleButton = new TextButton(property.getParameter().name(), 
        skin.get("toggle", TextButtonStyle.class));
    this.property = property;
    toggleButton.setName(property.getName()); 
    toggleButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        property.setValue(toggleButton.isChecked());
        syncWithModel();
      }
      
      @Override
      public boolean touchDown(InputEvent event, float localX, float localY, int pointer, int button) {
        ScienceEngine.selectParameter(property.getBody(), property.getParameter(),
            property.getValue(),
            (IScience2DView) toggleButton.getStage());
        return super.touchDown(event, localX, localY, pointer, button);
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