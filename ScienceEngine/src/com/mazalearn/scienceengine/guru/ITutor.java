package com.mazalearn.scienceengine.guru;

import java.util.List;

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
   * Reset components and configs to state at beginning
   * If childTutor is null, continue with current child if proper else 
   * go to first child. Otherwise use childTutor.
   */
  public void prepareToTeach(ITutor childTutor);

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
   * Called when teaching session is completed
   * @param success
   */
  public void done(boolean success);
  
  /**
   * @return list of child tutors or null if no children.
   */
  public List<ITutor> getChildTutors();
  /**
   * @return grouptype of this tutor
   */
  public GroupType getGroupType();

  /**
   * @return id of tutor - this must be unique within the level. 
   */
  public String getId();

  /**
   * @return parent tutor of this tutor. NULL only for Guru.
   */
  public ITutor getParentTutor();

  /**
   * Add to the time taken in this tutor
   * @param timeTaken
   */
  void addTimeSpent(float timeTaken);

  /**
   * @return time spent on this tutor
   */
  float getTimeSpent();

  /**
   * @return success percent on this tutor.
   * For a non-group tutor, this is 0 or 100.
   * For a group tutor, this is the percentage of children which are successful.
   */
  int getSuccessPercent();
}