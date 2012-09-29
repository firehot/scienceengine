package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.AbstractScience2DStage;
import com.mazalearn.scienceengine.core.view.IScience2DStage;
import com.mazalearn.scienceengine.experiments.ControlPanel;

public abstract class AbstractScience2DController implements
    IScience2DController {

  private ControlPanel controlPanel;
  private IScience2DModel science2DModel;
  private AbstractScience2DStage experimentView;
  private Skin skin;
  private String name;

  protected AbstractScience2DController(String name, Skin skin) {
    this.name = name;
    this.skin = skin;
  }
  
  protected void initialize(IScience2DModel science2DModel, 
      AbstractScience2DStage experimentView) {
    this.science2DModel = science2DModel;
    this.experimentView = experimentView;
    this.controlPanel = new ControlPanel(skin, this);
    experimentView.setControlPanel(this.controlPanel);
  }
  
  @Override
  public String getName() {
    return name;
  }
  
  @Override
  public IScience2DStage getView() {
    return experimentView;
  }

  @Override
  public IScience2DModel getModel() {
    return science2DModel;
  }

  @Override
  public ControlPanel getControlPanel() {
    return controlPanel;
  }
}
