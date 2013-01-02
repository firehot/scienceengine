package com.mazalearn.scienceengine.core.controller;

import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.ControlPanel;
import com.mazalearn.scienceengine.core.view.IScience2DView;

public interface IScience2DController {
  public IScience2DView getView();
  public IScience2DModel getModel();
  public ControlPanel getControlPanel();
  public String getName();
  int getLevel();
  /**
   * Reloads the level, reinitializing all components and configurations
   */
  void reload();
}
