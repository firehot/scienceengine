package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.view.CommandClickListener;
import com.mazalearn.scienceengine.core.view.IScience2DView;

//If property has only two values - we should show radio instead of select box
public class RadioControl implements IControl {
  private final IModelConfig<String> property;
  private final ButtonGroup radioGroup;
  private final CheckBox checkBox;

  public RadioControl(final IModelConfig<String> property, Skin skin) {
    this.radioGroup = new ButtonGroup();
    this.checkBox = new CheckBox(property.getName(), skin);
    radioGroup.add(checkBox);
    radioGroup.setMinCheckCount(1);
    radioGroup.setMaxCheckCount(1);
    this.checkBox.setName(property.getName());
    this.property = property;
    syncWithModel();
    // Set value when slider changes
    checkBox.addListener(new CommandClickListener() {
      @Override
      public void doCommand() {}
      
      @Override
      public boolean touchDown(InputEvent event, float localX, float localY, int pointer, int button) {
        ScienceEngine.selectParameter(property.getBody(), property.getParameter(), property.getValue(),
            (IScience2DView) checkBox.getStage());
        return super.touchDown(event, localX, localY, pointer, button);
      }
    });
  }

  public void syncWithModel() {
    
  }
  
  public boolean isActivated() {
    return property.isAvailable();
  }

  @Override
  public Actor getActor() {
    return checkBox;
  }
}