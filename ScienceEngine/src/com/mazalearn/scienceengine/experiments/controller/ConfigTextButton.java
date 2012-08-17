package com.mazalearn.scienceengine.experiments.controller;

import java.lang.reflect.Method;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.mazalearn.scienceengine.experiments.model.ExperimentModel;

/**
 * Utility class for experimentModel action buttons using reflection.
 *
 */
public class ConfigTextButton extends TextButton {
  public ConfigTextButton(final ExperimentModel experimentModel, final String action, 
      final Skin skin) {
    super(action, skin);
    // Find method for action by reflection and set onclick handler
    final Method actionMethod;
    try {
      actionMethod = experimentModel.getClass().getMethod(action.toLowerCase());
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Could not find action method");
    }
    this.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        try {
          actionMethod.invoke(experimentModel);
        } catch (Exception e) {
          throw new RuntimeException("Could not invoke action " + action);
        }
      }
    });
  }
}