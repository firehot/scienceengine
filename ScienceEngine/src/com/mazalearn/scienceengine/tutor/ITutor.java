package com.mazalearn.scienceengine.tutor;

import java.util.List;

import com.badlogic.gdx.graphics.Color;

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
    Constructed, Initialized, PreparedToTeach, Teaching, SystemFinished, UserFinished, Finished;
  };
  
  enum Type {
    Root(Color.CLEAR), 
    Guide(Color.YELLOW), 
    Challenge(Color.RED), 
    RapidFire(Color.MAGENTA),
    MCQ1(Color.MAGENTA),
    MCQ(Color.MAGENTA),
    KnowledgeUnit(Color.YELLOW),
    ParameterProber(Color.RED),
    Abstractor(Color.RED),
    Reviewer(Color.MAGENTA),
    FieldMagnitudeProber(Color.RED),
    FieldDirectionProber(Color.RED),
    LightProber(Color.RED),
    None(Color.CLEAR);
    
    Color color;

    private Type(Color color) {
      this.color = color;
    }
    
    public Color getColor() {
      return color;
    }
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
   * Called when a teaching session is completed
   */
  public void finish();
  
  /**
   * @return list of child tutors or null if no children.
   */
  public List<ITutor> getChildTutors();
  /**
   * @return type of this tutor
   */
  public Type getType();

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
   * @return time spent on this tutor
   */
  float getTimeSpent();

  /**
   * @return number of attempts on this tutor.
   */
  float getNumAttempts();

  /**
   * @return number of successful attempts on this tutor.
   */
  float getNumSuccesses();

  /**
   * @return number tracking the failures and type of failures.
   * Failure is expressed as XXXXXX 
   * Each X is the count MOD 8 that particular option was incorrectly checked
   *    either separately or in conjunction with other incorrect options.
   *    option is counted from left to right, starting with 0. (reversed octal)
   * The number of X's is the number of multiple choice options.
   * This does not accumulate at non-leaf levels.
   */
  float getFailureTracker();

  /**
   * @return attempted percent on this tutor.
   * For a non-group tutor, this is 0 or 100.
   * For a group tutor, this is the percentage of children attempted.
   */
  float getPercentAttempted();
}