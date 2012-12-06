package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.AbstractScience2DView;
import com.mazalearn.scienceengine.core.view.ControlPanel;
import com.mazalearn.scienceengine.core.view.IScience2DView;

public abstract class AbstractScience2DController implements
    IScience2DController {

  private ControlPanel controlPanel;
  private IScience2DModel science2DModel;
  private AbstractScience2DView science2DView;
  private Skin skin;
  private String name;
  private int level;

  protected AbstractScience2DController(String name, int level, Skin skin) {
    this.name = name;
    this.level = level;
    this.skin = skin;
  }
  
  protected void initialize(IScience2DModel science2DModel, 
      AbstractScience2DView science2DView) {
    this.science2DModel = science2DModel;
    this.science2DView = science2DView;
    this.controlPanel = new ControlPanel(skin, this);
    science2DView.setControlPanel(this.controlPanel);
  }
  
  @Override
  public String getName() {
    return name;
  }
  
  @Override
  public int getLevel() {
    return level;
  }
  
  @Override
  public IScience2DView getView() {
    return science2DView;
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
