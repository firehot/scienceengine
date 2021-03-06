package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.view.CommandClickListener;
import com.mazalearn.scienceengine.core.view.IScience2DView;

/**
 * Utility class for science2DModel text meter
 *
 */
public class TextMeter implements IControl {
  @SuppressWarnings("rawtypes")
  private final IModelConfig property;
  private final Label label;
   
  @SuppressWarnings("rawtypes")
  public TextMeter(final IModelConfig property, final Skin skin, String styleName) {
    this.label = new Label(property.getParameter().name(), skin);
    label.setColor(Color.YELLOW);
    this.property = property;
    label.setName(property.getName());
    label.addListener(new CommandClickListener() {
      @Override
      public void doCommand() {
        ScienceEngine.selectParameter(property.getBody(), property.getParameter(), 
            (Float) property.getValue(), (IScience2DView) label.getStage());
      }
    });
  }
  
  public Actor getActor() {
    return label;
  }

  @Override
  public void syncWithModel() {
    label.setText(String.valueOf(property.getValue()));
  }
  
  public boolean isActivated() {
    return property.isAvailable();
  }
}