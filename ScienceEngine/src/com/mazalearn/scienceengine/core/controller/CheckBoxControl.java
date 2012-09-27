package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class CheckBoxControl implements IControl {
  private final IModelConfig<Boolean> property;
  private final CheckBox checkBox;

  public CheckBoxControl(final IModelConfig<Boolean> property, Skin skin) {
    this.checkBox = new CheckBox(skin);
    this.property = property;
    syncWithModel();
    // Set value when slider changes
    checkBox.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        property.setValue(!property.getValue());
      }      
    });
  }

  public void syncWithModel() {
    checkBox.setChecked(property.getValue());
  }
  
  public boolean isAvailable() {
    return property.isAvailable();
  }

  @Override
  public Actor getActor() {
    return checkBox;
  }
}