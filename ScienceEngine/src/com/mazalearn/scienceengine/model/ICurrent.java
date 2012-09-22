package com.mazalearn.scienceengine.model;

public interface ICurrent {
  public interface Sink {
    public void updateCurrent(float amplitude);
  }
  public interface Source {
    public float getCurrent();
  }
}
