package com.mazalearn.scienceengine.controller;

import com.mazalearn.scienceengine.model.IExperimentModel;
import com.mazalearn.scienceengine.view.IExperimentView;

public interface IExperimentController {
  public IExperimentView getView();
  public IExperimentModel getModel();
  public Configurator getConfigurator();
  void enable(boolean enable);
}
