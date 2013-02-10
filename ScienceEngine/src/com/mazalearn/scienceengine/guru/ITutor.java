package com.mazalearn.scienceengine.guru;

/**
 * A Tutor can be a guide, a prober or an abstractor.
 * A Guide walks through an activity showing points to be observed.
 * A Prober challenges the understanding of the activity.
 * An abstractor explores the physical/mathematical abstraction behind the 
 * activity albeit qualitatively.
 * 
 * @author sridhar
 *
 */
public interface ITutor {

  /**
   * @return goal of this tutor
   */
  public String getGoal();

  /**
   * Prepare tutor to be run once.
   * Reinitialize should have been called before.
   * @param activate
   */
  public void activate(boolean activate);

  /**
   * Reinitialize tutor before a series of runs. 
   * Each individual run requires activate
   * @param probeMode
   */
  public void reinitialize(boolean probeMode);

  public String getHint();

  public int getSuccessScore();

  public int getFailureScore();

  public void checkProgress();

  /**
   * @return true iff has successfully completed all stages
   */
  public boolean hasSucceeded();

  /**
   * @return true iff will not be able to successfully complete all stages
   */
  public boolean hasFailed();

  /**
   * When tutor is successful, this method is called
   */
  void doSuccessActions();

  /**
   * Reset components and configs to state at beginning
   * TODO: what is difference between reset and reinitialize???
   */
  public void reset();

  /**
   * Pushes a goal onto the goal stack
   * @param goal
   */
  void pushGoal(String goal);

  /**
   * Remove topmost goal from goal stack
   */
  void popGoal();

}