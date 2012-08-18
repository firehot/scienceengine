package com.mazalearn.scienceengine.experiments.controller;

import java.lang.reflect.Method;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.ValueChangedListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.experiments.model.IExperimentModel;

/**
 * Utility class for iExperimentModel floating point sliders using reflection.
 *
 */
public class ConfigSlider extends Slider {
  final IExperimentModel iExperimentModel;
  final String property;
  final Method getter, setter;
  
  public ConfigSlider(IExperimentModel iExperimentModel, String property, 
      float low, float high, Skin skin) {
    super(low, high, (high - low)/10, skin);
    this.iExperimentModel = iExperimentModel;
    this.property = property;
    // Find getter and setter for property by reflection
    try {
      getter = iExperimentModel.getClass().getMethod("get" + property);
      setter = iExperimentModel.getClass().getMethod(
          "set" + property, new Class[] {float.class});
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Could not find getter or setter");
    }
    // Initialize slider value
    setValue(getVal());
    // Set value when slider changes
    setValueChangedListener(new ValueChangedListener() {
      @Override
      public void changed(Slider slider, float value) {
        setVal(value);
      }      
    });
  }
  
  float getVal() {
    try {
      return (Float) getter.invoke(iExperimentModel);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0;
  }
  
  void setVal(float value) {
    try {
      setter.invoke(iExperimentModel, value);
      Gdx.app.log(ScienceEngine.LOG, "Setting " + property + " to " + value);
    } catch (Exception e) {
      e.printStackTrace();
      Gdx.app.log(ScienceEngine.LOG, "Failed to set " + property + " to " + value);
    }     
  }
}