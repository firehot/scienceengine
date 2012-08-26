package com.mazalearn.scienceengine.experiments.controller;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.SelectionListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class ConfigSelectBox extends SelectBox implements IConfigElement {
  private final IConfig<String> property;

  public ConfigSelectBox(final IConfig<String> property, Skin skin) {
    super(getItems(property), skin);
    this.property = property;
    syncWithModel();
    // Set value when slider changes
    setSelectionListener(new SelectionListener() {
      @Override
      public void selected(Actor actor, int index, String value) {
        property.setValue(value);
      }      
    });
  }

  @SuppressWarnings("rawtypes")
  protected static String[] getItems(IConfig<String> property) {
    Object[] e = property.getEnums();
    String[] items = new String[e.length];
    for (int i = 0; i < e.length; i++) {
      items[i] = ((Enum) e[i]).name();
    }
    return items;
  }
  
  public void syncWithModel() {
    setSelection(property.getValue());
  }
  
  public boolean isAvailable() {
    return property.isAvailable();
  }
}