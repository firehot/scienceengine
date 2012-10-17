package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

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
    slider.setName(property.getDescription());
    // Set value when slider changes
    slider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        property.setValue(slider.getValue());
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