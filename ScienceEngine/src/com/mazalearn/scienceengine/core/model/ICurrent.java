package com.mazalearn.scienceengine.core.model;

public interface ICurrent {
  public interface Sink {
    public void updateCurrent(float amplitude);
  }
  public interface Source {
    public float getCurrent();
  }
}
