package com.mazalearn.scienceengine.guru;

import com.badlogic.gdx.utils.Array;

public interface ITutor {

  public String getTitle();

  public void activate(boolean activate);

  public void reinitialize(float x, float y, float width, float height, boolean probeMode);

  public String getHint();

  public int getSuccessScore();

  public int getFailureScore();

  public void checkProgress();

  public void initializeComponents(Array<?> components);

  public boolean isCompleted();

}