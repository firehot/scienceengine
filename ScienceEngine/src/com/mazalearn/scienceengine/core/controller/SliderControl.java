package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.view.IScience2DStage;

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
      public boolean touchDown(InputEvent event, float localX, float localY, int pointer, int button) {
        ScienceEngine.selectParameter(property.getParameter(), 
            (IScience2DStage) slider.getStage());
        return super.touchDown(event, localX, localY, pointer, button);
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