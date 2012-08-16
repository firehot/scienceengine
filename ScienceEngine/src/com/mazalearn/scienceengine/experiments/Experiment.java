package com.mazalearn.scienceengine.experiments;

public interface Experiment {
  public void reset();
  public void pause();
  public void resume();
  public boolean isPaused();
}
