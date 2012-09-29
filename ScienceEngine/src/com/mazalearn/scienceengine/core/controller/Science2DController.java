package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.core.model.IExperimentModel;
import com.mazalearn.scienceengine.core.view.Science2DStage;
import com.mazalearn.scienceengine.core.view.IExperimentView;
import com.mazalearn.scienceengine.experiments.ControlPanel;

public abstract class Science2DController implements
    IExperimentController {

  private ControlPanel controlPanel;
  private IExperimentModel experimentModel;
  private Science2DStage experimentView;
  private Skin skin;
  private String name;

  protected Science2DController(String name, Skin skin) {
    this.name = name;
    this.skin = skin;
  }
  
  protected void initialize(IExperimentModel experimentModel, 
      Science2DStage experimentView) {
    this.experimentModel = experimentModel;
    this.experimentView = experimentView;
    this.controlPanel = new ControlPanel(skin, this);
    experimentView.setControlPanel(this.controlPanel);
  }
  
  @Override
  public String getName() {
    return name;
  }
  
  @Override
  public IExperimentView getView() {
    return experimentView;
  }

  @Override
  public IExperimentModel getModel() {
    return experimentModel;
  }

  @Override
  public ControlPanel getControlPanel() {
    return controlPanel;
  }
}
