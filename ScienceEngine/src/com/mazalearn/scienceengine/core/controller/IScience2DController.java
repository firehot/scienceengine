package com.mazalearn.scienceengine.core.controller;

import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.ControlPanel;
import com.mazalearn.scienceengine.core.view.IScience2DStage;

public interface IScience2DController {
  public IScience2DStage getView();
  public IScience2DModel getModel();
  public ControlPanel getControlPanel();
  public String getName();
  int getLevel();
}
