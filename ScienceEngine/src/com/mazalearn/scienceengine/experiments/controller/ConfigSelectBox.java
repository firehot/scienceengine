package com.mazalearn.scienceengine.experiments.controller;

import java.lang.reflect.Method;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.SelectionListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.experiments.model.ExperimentModel;

public class ConfigSelectBox extends SelectBox {
  final ExperimentModel experimentModel;
  final String property;
  final Method getter, setter;

  public ConfigSelectBox(final ExperimentModel experimentModel, final String property, 
      final String[] items, final Skin skin) {
    super(items, skin);
    this.experimentModel = experimentModel;
    this.property = property;
    // Find getter and setter for property by reflection
    try {
      getter = experimentModel.getClass().getMethod("get" + property);
      setter = experimentModel.getClass().getMethod(
          "set" + property, new Class[] {String.class});
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Could not find getter or setter");
    }
    // Initialize selected item
    setSelection(getVal());
    // Set value when slider changes
    setSelectionListener(new SelectionListener() {
      @Override
      public void selected(Actor actor, int index, String value) {
        setVal(value);
      }      
    });
  }
  
  String getVal() {
    try {
      return (String) getter.invoke(experimentModel);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }
  
  void setVal(String value) {
    try {
      setter.invoke(experimentModel, value);
      Gdx.app.log(ScienceEngine.LOG, "Setting " + property + " to " + value);
    } catch (Exception e) {
      e.printStackTrace();
      Gdx.app.log(ScienceEngine.LOG, "Failed to set " + property + " to " + value);
    }     
  }
}