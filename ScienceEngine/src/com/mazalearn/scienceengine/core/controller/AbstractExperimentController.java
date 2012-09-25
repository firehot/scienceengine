package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.core.model.IExperimentModel;
import com.mazalearn.scienceengine.core.view.AbstractExperimentView;
import com.mazalearn.scienceengine.core.view.IExperimentView;
import com.mazalearn.scienceengine.experiments.ControlPanel;

public abstract class AbstractExperimentController implements
    IExperimentController {

  private ControlPanel controlPanel;
  private IExperimentModel experimentModel;
  private AbstractExperimentView experimentView;
  private Skin skin;
  private String name;

  protected AbstractExperimentController(String name, Skin skin) {
    this.name = name;
    this.skin = skin;
  }
  
  protected void initialize(IExperimentModel experimentModel, 
      AbstractExperimentView experimentView) {
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
  public ControlPanel getConfigurator() {
    return controlPanel;
  }
}
