package com.mazalearn.scienceengine.guru;


public interface ITutor {

  public String getGoal();

  public void activate(boolean activate);

  public void reinitialize(boolean probeMode);

  public String getHint();

  public int getSuccessScore();

  public int getFailureScore();

  public void checkProgress();

  // Has successfully completed all stages
  public boolean hasSucceeded();

  // Will not be able to successfully complete all stages
  public boolean hasFailed();

  // When tutor is successful, execute these actions
  void doSuccessActions();

  // Reset components and configs to state at beginning
  public void reset();

}