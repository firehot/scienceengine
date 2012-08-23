package com.mazalearn.scienceengine.experiments.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.mazalearn.scienceengine.experiments.model.IExperimentModel;

public class AbstractExperimentView extends Group implements IExperimentView {

  protected static final int PIXELS_PER_M = 8;
  protected boolean isPaused = false;
  protected final IExperimentModel experimentModel;

  public AbstractExperimentView(IExperimentModel experimentModel) {
    super();
    this.experimentModel = experimentModel;
  }

  @Override
  public void pause() {
    this.isPaused = true;
  }

  @Override
  public void resume() {
    this.isPaused = false;
  }

  @Override
  public boolean isPaused() {
    return isPaused;
  }
}