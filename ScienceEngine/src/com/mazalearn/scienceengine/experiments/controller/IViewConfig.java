package com.mazalearn.scienceengine.experiments.controller;

import com.badlogic.gdx.scenes.scene2d.Actor;

public interface IViewConfig {
  public void syncWithModel();
  public boolean isAvailable();
  public Actor getActor();
}
