package com.mazalearn.scienceengine.view;

import java.util.Map;

import com.badlogic.gdx.scenes.scene2d.Actor;

public interface IExperimentView {
  public void pause();
  public void resume();
  public boolean isPaused();
  public Map<String, Actor> getComponents();
}
