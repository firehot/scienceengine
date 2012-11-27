package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.view.IScience2DStage;
import com.mazalearn.scienceengine.core.view.StageComponent;

/**
 * Utility class for science2DModel action buttons using reflection.
 *
 */
public class ToggleButtonControl implements IControl {
  private final IModelConfig<Boolean> property;
  
  protected final TextButton toggleButton;
  
  public ToggleButtonControl(final IModelConfig<Boolean> property, final Skin skin) {
    this.toggleButton = new TextButton(property.getAttribute().name(), 
        skin.get("toggle", TextButtonStyle.class));
    this.property = property;
    toggleButton.setName(property.getName()); 
    toggleButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        property.setValue(toggleButton.isChecked());
      }
      
      @Override
      public boolean touchDown(InputEvent event, float localX, float localY, int pointer, int button) {
        IScience2DStage stage = (IScience2DStage) toggleButton.getStage();
        Label status = (Label) stage.findActor(StageComponent.Status.name());
        String component = "";
        if (ScienceEngine.getSelectedBody() != null) {
          component = ScienceEngine.getSelectedBody().getComponentType().toString() + " - ";
        }
        status.setText( component + 
            ScienceEngine.getMsg().getString("Help." + property.getAttribute().name()));
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