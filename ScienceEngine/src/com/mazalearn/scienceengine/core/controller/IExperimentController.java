package com.mazalearn.scienceengine.core.controller;

import com.mazalearn.scienceengine.core.model.IExperimentModel;
import com.mazalearn.scienceengine.core.view.IExperimentView;
import com.mazalearn.scienceengine.experiments.ControlPanel;

public interface IExperimentController {
  public IExperimentView getView();
  public IExperimentModel getModel();
  public ControlPanel getControlPanel();
  public String getName();
}
