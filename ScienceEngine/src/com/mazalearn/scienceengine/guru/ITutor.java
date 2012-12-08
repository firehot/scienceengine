package com.mazalearn.scienceengine.guru;

public interface ITutor {

  public abstract String getTitle();

  public abstract void activate(boolean activate);

  public abstract void reinitialize(float x, float y, float width, float height, boolean probeMode);

  public abstract String getHint();

}