package com.mazalearn.scienceengine.controller;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.model.IExperimentModel;
import com.mazalearn.scienceengine.view.AbstractExperimentView;
import com.mazalearn.scienceengine.view.IExperimentView;

public abstract class AbstractExperimentController implements
    IExperimentController {

  private Configurator configurator;
  private IExperimentModel experimentModel;
  private AbstractExperimentView experimentView;
  private Skin skin;

  protected AbstractExperimentController(Skin skin) {
    this.skin = skin;
  }
  
  protected void initialize(IExperimentModel experimentModel, 
      AbstractExperimentView experimentView, String name) {
    this.experimentModel = experimentModel;
    experimentModel.initializeConfigs();
    this.experimentView = experimentView;
    this.configurator = new Configurator(skin, experimentModel, experimentView, name);
    experimentView.addActor(this.configurator);
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
  public Configurator getConfigurator() {
    return configurator;
  }
}
