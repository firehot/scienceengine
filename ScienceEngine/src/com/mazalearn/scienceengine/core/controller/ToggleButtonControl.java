package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.view.CommandClickListener;
import com.mazalearn.scienceengine.core.view.IScience2DView;

/**
 * Utility class for science2DModel action buttons using reflection.
 *
 */
public class ToggleButtonControl implements IControl {
  private final IModelConfig<Boolean> property;
  
  protected final TextButton toggleButton;
  
  public ToggleButtonControl(final IModelConfig<Boolean> property, final Skin skin) {
    this(property, skin, "toggle");
  }
  
  public ToggleButtonControl(final IModelConfig<Boolean> property, final Skin skin, String styleName) {
    this.toggleButton = new TextButton(property.getParameter().name(), 
        skin.get(styleName == "default" ? "toggle" : styleName, TextButtonStyle.class)) {
      @Override
      public void draw(SpriteBatch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
      }
    };
    this.property = property;
    toggleButton.setName(property.getName()); 
    toggleButton.addListener(new CommandClickListener() {
      @Override
      public void doCommand() {
        property.setValue(toggleButton.isChecked());
        syncWithModel();
        ScienceEngine.selectParameter(property.getBody(), property.getParameter(),
            property.getValue(),
            (IScience2DView) toggleButton.getStage());
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
  
  public boolean isActivated() {
    return property.isAvailable();
  }
}