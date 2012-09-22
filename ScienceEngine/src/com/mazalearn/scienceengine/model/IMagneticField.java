package com.mazalearn.scienceengine.model;

import com.badlogic.gdx.math.Vector2;

public interface IMagneticField {

  public interface Consumer {
    Vector2 getPosition();
    void setBField(Vector2 magneticField);
    boolean isActive();
    void notifyFieldChange();
  }

  public interface Producer {
    Vector2 getBField(Vector2 location, Vector2 magneticField /* output */);
    boolean isActive();
  }

}
