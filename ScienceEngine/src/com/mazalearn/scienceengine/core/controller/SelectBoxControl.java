package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.view.CommandClickListener;
import com.mazalearn.scienceengine.core.view.IScience2DView;

public class SelectBoxControl implements IControl {
  private final IModelConfig<String> property;
  private final SelectBox selectBox;

  public SelectBoxControl(final IModelConfig<String> property, Skin skin, String styleName) {
    this.selectBox = new SelectBox (getItems(property), skin, styleName);
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
    selectBox.addListener(new CommandClickListener() {
        @Override
        public void doCommand() {
          ScienceEngine.selectParameter(property.getBody(), property.getParameter(),
              property.getValue(),
              (IScience2DView) selectBox.getStage());
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
  
  public boolean isActivated() {
    return property.isAvailable();
  }

  @Override
  public Actor getActor() {
    return selectBox;
  }
}