package com.mazalearn.scienceengine.experiments.controller;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;

/**
 * Utility class for iExperimentModel floating point sliders using reflection.
 *
 */
public class ConfigSlider extends Slider implements IViewConfig {
  private final IModelConfig<Float> property;
  
  public ConfigSlider(final IModelConfig<Float> property, Skin skin) {
    super(property.getLow(), property.getHigh(), 
        (property.getHigh() - property.getLow())/10, skin);
    this.property = property;
    syncWithModel();
    // Set value when slider changes
    setValueChangedListener(new ValueChangedListener() {
      @Override
      public void changed(Slider slider, float value) {
        property.setValue(value);
      }      
    });
  }
  
  @Override
  public void syncWithModel() {
    this.setValue(property.getValue());
  }
 
  public boolean isAvailable() {
    return property.isAvailable();
  }
}