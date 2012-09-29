package com.mazalearn.scienceengine.core.controller;

import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.IScience2DStage;
import com.mazalearn.scienceengine.experiments.ControlPanel;

public interface IScience2DController {
  public IScience2DStage getView();
  public IScience2DModel getModel();
  public ControlPanel getControlPanel();
  public String getName();
}
