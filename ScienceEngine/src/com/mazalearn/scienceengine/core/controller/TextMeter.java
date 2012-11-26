package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.view.IScience2DStage;
import com.mazalearn.scienceengine.core.view.StageComponent;

/**
 * Utility class for science2DModel text meter
 *
 */
public class TextMeter implements IControl {
  @SuppressWarnings("rawtypes")
  private final IModelConfig property;
  
  protected final Label label;
  
  @SuppressWarnings("rawtypes")
  public TextMeter(final IModelConfig property, final Skin skin) {
    this.label = new Label(property.getAttribute().name(), skin);
    label.setColor(Color.YELLOW);
    this.property = property;
    label.setName(property.getName());
    label.addListener(new ClickListener() {
      @Override
      public boolean touchDown(InputEvent event, float localX, float localY, int pointer, int button) {
        IScience2DStage stage = (IScience2DStage) label.getStage();
        Label status = (Label) stage.findActor(StageComponent.Status.name());
        String component = "";
        if (ScienceEngine.getSelectedBody() != null) {
          component = ScienceEngine.getSelectedBody().getComponentType().name() + " - ";
        }
        status.setText( component + 
            ScienceEngine.getMsg().getString("Help." + property.getAttribute().name()));
        return super.touchDown(event, localX, localY, pointer, button);
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
  
  public boolean isAvailable() {
    return property.isAvailable();
  }
}