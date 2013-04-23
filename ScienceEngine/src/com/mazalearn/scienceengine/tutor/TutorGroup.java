package com.mazalearn.scienceengine.tutor;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.lang.Expr;
import com.mazalearn.scienceengine.core.lang.Parser;
import com.mazalearn.scienceengine.core.lang.SyntaxException;
import com.mazalearn.scienceengine.core.lang.Variable;
import com.mazalearn.scienceengine.core.model.IComponentType;

public class TutorGroup extends AbstractTutor {
  
  private static final int RAPID_FIRE_MAX = 10;
  private static final int REVIEWER_MAX = 50;
  private List<ITutor> childTutors = Collections.emptyList();
  private ITutor currentTutor;
  
  private float[] tutorBeginTime;
  private int tutorIndex = -1;

  private Expr successActions;
  private Set<Variable> variables;
  private int numChildren;


    
  public TutorGroup(IScience2DController science2DController, TutorType tutorType, Topic topic,
      ITutor parent, String goal, String id, Array<?> components, Array<?> configs, 
      String[] hints, String[] explanation, String[] refs) {
    super(science2DController, tutorType, topic, parent, goal, id, components, configs, 
        hints, explanation, refs);
  }
  
  @Override
  public void systemReadyToFinish(boolean success) {
    // If this tutor has been unsuccessful, it informs parent.
    if (!success) {
      super.systemReadyToFinish(false);
      return;
    }
    // Move on to next stage
    if (++tutorIndex == numChildren) {
      // Goto first tutor which has not been successfully done, if any
      for (tutorIndex = 0; tutorIndex < numChildren; tutorIndex++) {
        if (childTutors.get(tutorIndex).getStats()[ITutor.PERCENT_PROGRESS] < 100) {
          break;
        }
      }
      // If all children done, we are ready to finish
      if (tutorIndex == numChildren || 
             getType() == TutorType.RapidFire || 
             getType() == TutorType.Reviewer || 
             getType() == TutorType.Root) {
        this.success = true;
        // No user input required for group tutors
        this.state = State.UserFinished;
        super.systemReadyToFinish(true);
        doSuccessActions();
        return;
      }
    }
    currentTutor = childTutors.get(tutorIndex);
    teach();
  }

  /* (non-Javadoc)
   * @see com.mazalearn.scienceengine.tutor.AbstractTutor#teach()
   */
  @Override
  public void teach() {
    super.teach();
    tutorBeginTime[tutorIndex] = ScienceEngine.getTime();
    ScienceEngine.setProbeMode(false);
    currentTutor.prepareToTeach(null);
    
    IComponentType tutorType = currentTutor.getType();
    if (tutorType == TutorType.Challenge) {
      tutorHelper.doChallengeAnimation(currentTutor);
    } else if (tutorType == TutorType.RapidFire || tutorType == TutorType.Reviewer) {
      tutorHelper.doRapidFireAnimation(currentTutor);
    } else {
      currentTutor.teach();
    }
  }

  /* (non-Javadoc)
   * @see com.mazalearn.scienceengine.tutor.AbstractTutor#prepareToTeach(ITutor)
   */
  @Override
  public void prepareToTeach(ITutor childTutor) {
     
    if (getType() == TutorType.RapidFire || getType() == TutorType.Reviewer) {
      randomizeChildTutors();
      tutorIndex = 0;
      currentTutor = childTutors.get(tutorIndex);
      super.prepareToTeach(currentTutor);
      return;
    } 
    
    if (childTutor != null) {
      tutorIndex = childTutors.indexOf(childTutor);
    }
    if (tutorIndex < 0 || tutorIndex >= numChildren) {
      // Find out where we last left off.
      for (tutorIndex = 0; tutorIndex < numChildren; tutorIndex++) {
        if (childTutors.get(tutorIndex).getStats()[ITutor.PERCENT_PROGRESS] < 100) break;
      }
      if (tutorIndex == numChildren) tutorIndex = 0;
    }
    currentTutor = childTutors.get(tutorIndex);
    super.prepareToTeach(currentTutor);
  }

  private void randomizeChildTutors() {
    Utils.shuffle(childTutors);
    numChildren = Math.min(childTutors.size(), 
        (getType() == TutorType.RapidFire) ? RAPID_FIRE_MAX : REVIEWER_MAX);
  }
  
  @Override
  public void checkProgress() {
    if (currentTutor == null) return;
    currentTutor.checkProgress();
  }
  
  public void initialize(List<ITutor> childTutors, String successActionsString) {
    this.childTutors = childTutors;
    this.clear(); // Clear all actors
    for (ITutor childTutor: childTutors) {
      this.addActor((AbstractTutor) childTutor);
      childTutor.setParentTutor(this);
    }
    numChildren = childTutors.size();
    // End timeLimit of stage is begin timeLimit of stage i+1. So we need 1 extra
    this.tutorBeginTime = new float[childTutors.size() + 1];
    if (successActionsString != null) {
      Parser parser = tutorHelper.createParser();
      try {
        this.successActions = parser.parseString(successActionsString);
      } catch (SyntaxException e) {
        if (ScienceEngine.DEV_MODE == DevMode.DEBUG) e.printStackTrace();
        throw new RuntimeException(e);
      }
      this.variables = parser.getVariables();
    }
  }

  @Override
  public List<ITutor> getChildTutors() {
    return childTutors;
  }
  
  private void doSuccessActions() {
    if (successActions == null) return;
    science2DController.getModel().bindParameterValues(variables);
    successActions.bvalue();    
  }
  
  @Override
  public void recordStats() {
    // Update all stats
    float timeSpent = 0;
    int numAttempted = 0;
    float percentProgress = 0;
    int numSuccesses = 0;
    float points = 0;
    for (int i = 0; i < numChildren; i++) {
      ITutor child = childTutors.get(i);
      timeSpent += child.getStats()[ITutor.TIME_SPENT];
      if (child.getStats()[ITutor.NUM_ATTEMPTS] > 0) numAttempted++;
      percentProgress += child.getStats()[ITutor.PERCENT_PROGRESS];
      if (child.getStats()[ITutor.NUM_SUCCESSES] > 0) numSuccesses++;
      points += child.getStats()[ITutor.POINTS];
    }
    stats[ITutor.TIME_SPENT] = timeSpent;   
    stats[ITutor.NUM_ATTEMPTS] = numAttempted;
    stats[ITutor.PERCENT_PROGRESS] = percentProgress / numChildren;
    stats[ITutor.NUM_SUCCESSES] = numSuccesses;
    stats[ITutor.POINTS] = points;
    
    // Save stats into profile
    tutorHelper.getProfile().saveStats(stats, getId());
    parent.recordStats();
  }
}