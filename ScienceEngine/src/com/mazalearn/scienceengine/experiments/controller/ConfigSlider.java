package com.mazalearn.scienceengine.experiments.controller;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.ValueChangedListener;

/**
 * Utility class for experimentModel floating point sliders using reflection.
 *
 */
public class ConfigSlider implements IViewConfig {
  private final IModelConfig<Float> property;
  private final Slider slider;
  
  public ConfigSlider(final IModelConfig<Float> property, Skin skin) {
    this.slider = new Slider(property.getLow(), property.getHigh(), 
        (property.getHigh() - property.getLow())/10, skin);
    this.property = property;
    syncWithModel();
    // Set value when slider changes
    slider.setValueChangedListener(new ValueChangedListener() {
      @Override
      public void changed(Slider slider, float value) {
        property.setValue(value);
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