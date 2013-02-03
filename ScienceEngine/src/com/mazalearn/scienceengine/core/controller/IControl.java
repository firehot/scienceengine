package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.scenes.scene2d.Actor;

public interface IControl {
  public void syncWithModel();
  public boolean isActivated();
  public Actor getActor();
}
