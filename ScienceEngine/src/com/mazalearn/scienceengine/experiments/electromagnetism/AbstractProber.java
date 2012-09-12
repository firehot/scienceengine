package com.mazalearn.scienceengine.experiments.electromagnetism;

import com.badlogic.gdx.scenes.scene2d.Group;

public abstract class AbstractProber extends Group {
  public abstract void activate(boolean activate);
  public abstract String getTitle();
}
