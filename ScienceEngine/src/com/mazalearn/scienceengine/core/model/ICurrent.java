package com.mazalearn.scienceengine.core.model;

public interface ICurrent {
  public interface Sink {
    public void setCurrent(float current);
  }
  public interface Source {
    public float getCurrent();
  }
}
