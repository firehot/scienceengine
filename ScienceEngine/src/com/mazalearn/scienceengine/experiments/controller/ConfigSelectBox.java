package com.mazalearn.scienceengine.experiments.controller;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.SelectionListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class ConfigSelectBox extends SelectBox implements IConfigElement {
  final IConfig<String> property;

  @SuppressWarnings("rawtypes")
  public ConfigSelectBox(IConfig<String> property, Enum[] e, Skin skin) {
    super(getItems(e), skin);
    this.property = property;
    syncWithModel();
    // Set value when slider changes
    setSelectionListener(new SelectionListener() {
      @Override
      public void selected(Actor actor, int index, String value) {
        setVal(value);
      }      
    });
  }

  @SuppressWarnings("rawtypes")
  protected static String[] getItems(Object[] e) {
    String[] items = new String[e.length];
    for (int i = 0; i < e.length; i++) {
      items[i] = ((Enum) e[i]).name();
    }
    return items;
  }
  
  String getVal() { return property.getValue(); }
  
  void setVal(String value) { property.setValue(value); }
  
  public void syncWithModel() {
    setSelection(getVal());
  }
}