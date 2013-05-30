package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.view.CommandClickListener;
import com.mazalearn.scienceengine.core.view.IScience2DView;

/**
 * Utility class for science2DModel floating point sliders using reflection.
 *
 */
public class SliderControl implements IControl {
  private final IModelConfig<Float> property;
  private final Slider slider;
  private static final float DELTA = 5;
  
  public SliderControl(final IModelConfig<Float> property, final Skin skin, String styleName) {
    if (property.getLow() < 0 && property.getHigh() > 0) {
      // Add a 0 bar to the slider
      this.slider = new Slider(property.getLow(), property.getHigh(), 
          (property.getHigh() - property.getLow())/10, false, skin) {
        BitmapFont font = skin.getFont("default-font");
        public void draw(SpriteBatch batch, float parentAlpha) {
          super.draw(batch, parentAlpha);
          // Show 0 in slider at right place
          float w0 = - property.getLow() / (property.getHigh() - property.getLow()) * getWidth();
          font.draw(batch, "I", getX() + w0, getY() + DELTA * 4);
        }
      };
    } else {
      this.slider = new Slider(property.getLow(), property.getHigh(), 
          (property.getHigh() - property.getLow())/10, false, skin);
    };
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
    slider.addListener(new CommandClickListener() {
      @Override
      public void doCommand() {
        ScienceEngine.selectParameter(property.getBody(), property.getParameter(),
            property.getValue(),
            (IScience2DView) slider.getStage());
      }
    });
  }
  
  @Override
  public void syncWithModel() {
    Float value = property.getValue();
    // Keep property value in min,max range.
    if (value < property.getLow()) property.setValue(value = property.getLow());
    if (value > property.getHigh()) property.setValue(value = property.getHigh());

    slider.setValue(value);
  }
 
  public boolean isActivated() {
    return property.isAvailable();
  }

  @Override
  public Actor getActor() {
    return slider;
  }
}