package com.mazalearn.scienceengine.experiments.config;

import java.lang.reflect.Method;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.ValueChangedListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.experiments.Experiment;

/**
 * Utility class for experiment floating point sliders using reflection.
 *
 */
public class ConfigSlider extends Slider {
  final Experiment experiment;
  final String property;
  final Method getter, setter;
  
  public ConfigSlider(Experiment experiment, String property, 
      float low, float high, Skin skin) {
    super(low, high, (high - low)/10, skin);
    this.experiment = experiment;
    this.property = property;
    // Find getter and setter for property by reflection
    try {
      getter = experiment.getClass().getMethod("get" + property);
      setter = experiment.getClass().getMethod(
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
      return (Float) getter.invoke(experiment);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0;
  }
  
  void setVal(float value) {
    try {
      setter.invoke(experiment, value);
      Gdx.app.log(ScienceEngine.LOG, "Setting " + property + " to " + value);
    } catch (Exception e) {
      e.printStackTrace();
      Gdx.app.log(ScienceEngine.LOG, "Failed to set " + property + " to " + value);
    }     
  }
}