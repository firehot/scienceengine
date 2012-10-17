package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class SelectBoxControl implements IControl {
  private final IModelConfig<String> property;
  private final SelectBox selectBox;

  public SelectBoxControl(final IModelConfig<String> property, Skin skin) {
    this.selectBox = new SelectBox (getItems(property), skin);
    this.property = property;
    syncWithModel();
    selectBox.setName(property.getDescription());
    // Set value when slider changes
    selectBox.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        property.setValue(selectBox.getSelection());
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