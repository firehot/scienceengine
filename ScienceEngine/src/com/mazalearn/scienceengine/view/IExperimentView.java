package com.mazalearn.scienceengine.view;

import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Actor;

public interface IExperimentView {
  public void pause();
  public void resume();
  public boolean isPaused();
  public List<Actor> getActors();
}
