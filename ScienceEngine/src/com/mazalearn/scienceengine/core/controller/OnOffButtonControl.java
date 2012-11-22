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
public class OnOffButtonControl implements IControl {
  private final IModelConfig<Boolean> property;
  
  protected final TextButton toggleButton;
  
  public OnOffButtonControl(final IModelConfig<Boolean> property, final Skin skin) {
    this.toggleButton = new TextButton(property.getName(), 
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
      public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        super.enter(event, x, y, pointer, fromActor);
        IScience2DStage stage = (IScience2DStage) toggleButton.getStage();
        Label status = (Label) stage.findActor(StageComponent.Status.name());
        status.setText(ScienceEngine.getMsg().getString("Help." + property.getAttribute().name()));
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