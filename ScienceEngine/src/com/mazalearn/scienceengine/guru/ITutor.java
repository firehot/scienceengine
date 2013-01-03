package com.mazalearn.scienceengine.guru;


public interface ITutor {

  public String getGoal();

  public void activate(boolean activate);

  public void reinitialize(float x, float y, float width, float height, boolean probeMode);

  public String getHint();

  public int getSuccessScore();

  public int getFailureScore();

  public void checkProgress();

  // Has successfully completed all stages
  public boolean hasSucceeded();

  // Will not be able to successfully complete all stages
  public boolean hasFailed();

}