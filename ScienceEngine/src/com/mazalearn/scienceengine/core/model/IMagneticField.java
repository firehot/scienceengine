package com.mazalearn.scienceengine.core.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public interface IMagneticField {

  public interface Consumer {
    Vector2 getPosition();
    void setBField(Vector3 magneticField);
    boolean isActive();
    void notifyFieldChange();
  }

  public interface Producer {
    Vector3 getBField(Vector2 location, Vector3 magneticField /* output */);
    boolean isActive();
    Vector2 getPosition(); // TODO: remove this being used for dedup
  }

}
