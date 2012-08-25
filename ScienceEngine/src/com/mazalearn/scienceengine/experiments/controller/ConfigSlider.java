package com.mazalearn.scienceengine.experiments.controller;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;

/**
 * Utility class for iExperimentModel floating point sliders using reflection.
 *
 */
public class ConfigSlider extends Slider implements IConfigElement {
  final IConfig<Float> property;
  
  public ConfigSlider(IConfig<Float> property, float low, float high, Skin skin) {
    super(low, high, (high - low)/10, skin);
    this.property = property;
    syncWithModel();
    // Set value when slider changes
    setValueChangedListener(new ValueChangedListener() {
      @Override
      public void changed(Slider slider, float value) {
        setVal(value);
      }      
    });
  }
  
  float getVal() { return property.getValue(); }

  void setVal(float value) { property.setValue(value); }

  @Override
  public void syncWithModel() {
    this.setValue(getVal());
  }
}