package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.view.IScience2DStage;
import com.mazalearn.scienceengine.core.view.StageComponent;

public class SelectBoxControl implements IControl {
  private final IModelConfig<String> property;
  private final SelectBox selectBox;

  public SelectBoxControl(final IModelConfig<String> property, Skin skin) {
    this.selectBox = new SelectBox (getItems(property), skin);
    this.property = property;
    syncWithModel();
    selectBox.setName(property.getName());
    // Set value when slider changes
    selectBox.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        property.setValue(selectBox.getSelection());
      }      
    });
    selectBox.addListener(new ClickListener() {   
      @Override
      public boolean touchDown(InputEvent event, float localX, float localY, int pointer, int button) {
        IScience2DStage stage = (IScience2DStage) selectBox.getStage();
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

  @SuppressWarnings("rawtypes")
  protected static String[] getItems(IModelConfig<String> property) {
    Object[] e = property.getList();
    String[] items = new String[e.length];
    for (int i = 0; i < e.length; i++) {
      items[i] = ((Enum) e[i]).name();
    }
    return items;
  }
  
  public void syncWithModel() {
    selectBox.setSelection(property.getValue());
  }
  
  public boolean isAvailable() {
    return property.isAvailable();
  }

  @Override
  public Actor getActor() {
    return selectBox;
  }
}