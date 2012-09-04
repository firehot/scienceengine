package com.mazalearn.scienceengine.controller;

import java.util.Map;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mazalearn.scienceengine.model.IExperimentModel;
import com.mazalearn.scienceengine.view.IExperimentView;

public interface IExperimentController {
  public Map<String, Actor> getComponents();
  public IExperimentView getView();
  public IExperimentModel getModel();
  public Configurator getConfigurator();
  void enable(boolean enable);
}
