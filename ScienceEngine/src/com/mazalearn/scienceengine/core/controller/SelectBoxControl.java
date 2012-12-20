package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.view.IScience2DView;

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
        property.setValue(selectBox.getSelection());
      }      
    });
    selectBox.addListener(new ClickListener() {   
      @Override
      public boolean touchDown(InputEvent event, float localX, float localY, int pointer, int button) {
        ScienceEngine.selectParameter(property.getParameter(),
            property.getValue(),
            (IScience2DView) selectBox.getStage());
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