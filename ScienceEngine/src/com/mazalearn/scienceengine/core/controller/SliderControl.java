package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.view.IScience2DStage;
import com.mazalearn.scienceengine.core.view.StageComponent;

/**
 * Utility class for science2DModel floating point sliders using reflection.
 *
 */
public class SliderControl implements IControl {
  private final IModelConfig<Float> property;
  private final Slider slider;
  
  public SliderControl(final IModelConfig<Float> property, Skin skin) {
    this.slider = new Slider(property.getLow(), property.getHigh(), 
        (property.getHigh() - property.getLow())/10, false, skin);
    this.property = property;
    syncWithModel();
    slider.setName(property.getName());
    // Set value when slider changes
    slider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        property.setValue(slider.getValue());
      }      
    });
    slider.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
      }
      
      @Override
      public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        super.enter(event, x, y, pointer, fromActor);
        IScience2DStage stage = (IScience2DStage) slider.getStage();
        Label status = (Label) stage.findActor(StageComponent.Status.name());
        status.setText(ScienceEngine.getMsg().getString("Help." + property.getAttribute().name()));
      }
    });
  }
  
  @Override
  public void syncWithModel() {
    slider.setValue(property.getValue());
  }
 
  public boolean isAvailable() {
    return property.isAvailable();
  }

  @Override
  public Actor getActor() {
    return slider;
  }
}