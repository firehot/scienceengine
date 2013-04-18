package com.mazalearn.scienceengine.tutor;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.mazalearn.scienceengine.core.model.IComponentType;

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

  enum State {
    Constructed, Initialized, PreparedToTeach, Teaching, SystemFinished, UserFinished, Finished, Aborted;
  };
  
  public interface ITutorType extends IComponentType {
    public Color getColor();

    public int getSuccessPoints();

    public int getFailurePoints();
  }

  public static final int NUM_ATTEMPTS = 0;
  public static final int NUM_SUCCESSES = 1;
  /**
   * Success percent on this tutor.
   * For a non-group tutor, this is 0 or 100.
   * For a group tutor, this is the percentage of children attempted successfully.
   */
  public static final int PERCENT_PROGRESS = 2;
  public static final int TIME_SPENT = 3;
  /**
   * Number tracking the failures and type of failures.
   * Failure is expressed as XXXXXX 
   * Each X is the count MOD 8 that particular option was incorrectly checked
   *    either separately or in conjunction with other incorrect options.
   *    option is counted from left to right, starting with 0. (reversed octal)
   * The number of X's is the number of multiple choice options.
   * This does not accumulate at non-leaf levels.
   */
  public static final int FAILURE_TRACKER = 4;
  public static final int NUM_STATS = 6;
  public static final int POINTS = 5;
  
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
   * End the teaching session.
   * May be called even when userReadyToFinish and systemReadyToFinish have
   * not been called - indicating premature exit.
   */
  public void finish();
  
  /**
   * @return list of child tutors or null if no children.
   */
  public List<ITutor> getChildTutors();
  
  /**
   * @return type of this tutor
   */
  public ITutorType getType();

  /**
   * @return id of tutor - this must be unique within the level. 
   */
  public String getId();

  /**
   * @return parent tutor of this tutor. NULL only for Guru.
   */
  public ITutor getParentTutor();

  /**
   * @return Current state of the tutor
   */
  public State getState();

  /**
   * Tutor can delegate to a child tutor or another component for part of 
   * the teaching. The delegatee reports back through this method along with
   * an indication of whether delegatee was successful.
   * @param success
   */
  void systemReadyToFinish(boolean success);

  /**
   * User has finished actions on this tutor.
   */
  public void userReadyToFinish();

  /**
   * Add to the time taken in this tutor. This is always cumulative.
   * @param timeTaken
   */
  void addTimeSpent(float timeTaken);

  /**
   * calculate and record statistics for this tutor
   */
  void recordStats();
  
  /**
   * get statistics for this tutor
   */
  public float[] getStats();
  
  /**
   * Abort current tutor - in whatever state it is.
   */
  public void abort();

  /**
   * Explanations for the specific thing taught by tutor, if any - may be empty
   * @return
   */
  public String[] getExplanation();

  /**
   * References for prerequisite concepts taught by this tutor - may be empty
   * @return
   */
  public String[] getRefs();

  public void setParentTutor(ITutor tutor);
}