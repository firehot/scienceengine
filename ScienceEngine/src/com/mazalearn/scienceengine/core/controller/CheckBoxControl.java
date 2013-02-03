package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.view.IScience2DView;

public class CheckBoxControl implements IControl {
  private final IModelConfig<Boolean> property;
  private final CheckBox checkBox;

  public CheckBoxControl(final IModelConfig<Boolean> property, Skin skin) {
    this.checkBox = new CheckBox(property.getName(), skin);
    this.checkBox.setName(property.getName());
    this.property = property;
    syncWithModel();
    // Set value when slider changes
    checkBox.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        property.setValue(!property.getValue());
      }
      
      @Override
      public boolean touchDown(InputEvent event, float localX, float localY, int pointer, int button) {
        ScienceEngine.selectParameter(property.getBody(), property.getParameter(), property.getValue(),
            (IScience2DView) checkBox.getStage());
        return super.touchDown(event, localX, localY, pointer, button);
      }
    });
  }

  public void syncWithModel() {
    checkBox.setChecked(property.getValue());
  }
  
  public boolean isActivated() {
    return property.isAvailable();
  }

  @Override
  public Actor getActor() {
    return checkBox;
  }
}