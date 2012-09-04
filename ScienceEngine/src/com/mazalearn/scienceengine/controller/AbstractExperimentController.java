package com.mazalearn.scienceengine.controller;

import java.util.Map;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.model.IExperimentModel;
import com.mazalearn.scienceengine.view.IExperimentView;

public abstract class AbstractExperimentController implements
    IExperimentController {

  private Configurator configurator;
  private IExperimentModel experimentModel;
  private IExperimentView experimentView;
  private Skin skin;

  protected AbstractExperimentController(Skin skin) {
    this.skin = skin;
  }
  
  protected void initialize(IExperimentModel experimentModel, IExperimentView experimentView) {
    this.experimentModel = experimentModel;
    this.experimentView = experimentView;
    this.configurator = new Configurator(skin, experimentModel, experimentView);    
  }
  
  @Override
  public Map<String, Actor> getComponents() {
    return experimentView.getComponents();
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
  
  @Override
  public void enable(boolean enable) {
    experimentModel.enable(enable);
  }

}
