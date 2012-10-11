package com.mazalearn.scienceengine.core.model;

import com.badlogic.gdx.math.Vector2;

public interface ICurrent {
  // Only components implementing this interface can participate in circuits
  public interface CircuitElement {
    public Vector2 getFirstTerminalPosition();
    public Vector2 getSecondTerminalPosition();
  }
  
  // Current sinks
  public interface Sink extends CircuitElement {
    public void setCurrent(float current);
  }
  
  // Current sources
  public interface Source extends CircuitElement {
    public float getCurrent();
  }
}
