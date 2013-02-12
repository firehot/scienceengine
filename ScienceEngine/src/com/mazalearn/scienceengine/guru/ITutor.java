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

  enum GroupType {
    Root, Guide, Challenge, None;
  }
  
  /**
   * @return goal of this tutor
   */
  public String getGoal();

  /**
   * Prepare tutor before a session. 
   */
  public void prepareToTeach();

  /**
   * Teach session.
   * prepareToTeach should have been called before.
   */
  public void teach();

  /**
   * @return hint for current session, if any. Maybe null.
   */
  public String getHint();

  /**
   * check progress???
   */
  public void checkProgress();

  /**
   * Reset components and configs to state at beginning
   * TODO: what is difference between reset and prepareToTeach???
   */
  public void reset();

  /**
   * Called when teaching session is completed
   * @param success
   */
  public void done(boolean success);
  
  /**
   * @return grouptype of this tutor
   */
  public GroupType getGroupType();
}