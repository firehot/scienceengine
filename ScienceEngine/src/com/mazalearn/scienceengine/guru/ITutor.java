package com.mazalearn.scienceengine.guru;

import com.badlogic.gdx.utils.Array;

public interface ITutor {

  public String getTitle();

  public void activate(boolean activate);

  public void reinitialize(float x, float y, float width, float height, boolean probeMode);

  public String getHint();

  public int getDeltaSuccessScore();

  public int getDeltaFailureScore();

  public void checkProgress();

  public void initializeComponents(Array<?> components);

  public boolean isCompleted();

}