package com.mazalearn.scienceengine.tutor;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.lang.Expr;
import com.mazalearn.scienceengine.core.lang.Parser;
import com.mazalearn.scienceengine.core.lang.SyntaxException;
import com.mazalearn.scienceengine.core.lang.Variable;

public class TutorGroup extends AbstractTutor {
  
  private List<ITutor> childTutors = Collections.emptyList();
  private ITutor currentTutor;
  
  private float[] tutorBeginTime;
  private int tutorIndex = -1;

  private Expr successActions;
  private Set<Variable> variables;


    
  public TutorGroup(IScience2DController science2DController, ITutor parent,
      String goal, String id, Array<?> components, Array<?> configs, 
      int successPoints, int failurePoints, String[] hints) {
    super(science2DController, parent, goal, id, components, configs, successPoints, failurePoints, hints);
  }
  
  @Override
  public void systemReadyToFinish(boolean success) {
    // If this tutor has been unsuccessful, it informs parent.
    if (!success) {
      super.systemReadyToFinish(false);
      return;
    }
    // Move on to next stage
    if (++tutorIndex == childTutors.size()) {
      if (getPercentAttempted() == 100) {
        this.success = true;
        // No user input required for group tutors
        this.state = State.UserFinished;
        super.systemReadyToFinish(true);
        doSuccessActions();
        return;
      }
      // Goto first tutor which has not been successfully done
      for (tutorIndex = 0; tutorIndex < childTutors.size(); tutorIndex++) {
        if (childTutors.get(tutorIndex).getPercentAttempted() < 100) {
          break;
        }
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
    switch(currentTutor.getGroupType()) {
    case Challenge: guru.doChallengeAnimation(currentTutor); break;
    case RapidFire: guru.doRapidFireAnimation(currentTutor); break;
    default: currentTutor.teach(); break;
    }
  }

  /* (non-Javadoc)
   * @see com.mazalearn.scienceengine.tutor.AbstractTutor#prepareToTeach(ITutor)
   */
  @Override
  public void prepareToTeach(ITutor childTutor) {
    if (childTutor != null) {
      tutorIndex = childTutors.indexOf(childTutor);
    }
    if (tutorIndex < 0 || tutorIndex >= childTutors.size()) {
      // Find out where we last left off.
      for (tutorIndex = 0; tutorIndex < childTutors.size(); tutorIndex++) {
        if (childTutors.get(tutorIndex).getNumAttempts() < 1) break;
      }
      if (tutorIndex == childTutors.size()) tutorIndex = 0;
    }
    currentTutor = childTutors.get(tutorIndex);
    super.prepareToTeach(currentTutor);
  }
  
  @Override
  public void checkProgress() {
    if (currentTutor == null) return;
    currentTutor.checkProgress();
  }
  
  public void initialize(String groupType, List<ITutor> childTutors, String successActionsString) {
    this.setGroupType(GroupType.valueOf(groupType));
    this.childTutors = childTutors;
    for (ITutor childTutor: childTutors) {
      this.addActor((AbstractTutor) childTutor);
    }
    if (getGroupType() == GroupType.RapidFire) {
      // Shuffle child tutors
      Utils.shuffle(childTutors);
    }
    // End timeLimit of stage is begin timeLimit of stage i+1. So we need 1 extra
    this.tutorBeginTime = new float[childTutors.size() + 1];
    if (successActionsString != null) {
      Parser parser = guru.createParser();
      try {
        this.successActions = parser.parseString(successActionsString);
      } catch (SyntaxException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
      this.variables = parser.getVariables();
    }
  }

  @Override
  public List<ITutor> getChildTutors() {
    if (getGroupType() == GroupType.RapidFire) return null;
    return childTutors;
  }
  
  private void doSuccessActions() {
    if (successActions == null) return;
    science2DController.getModel().bindParameterValues(variables);
    successActions.bvalue();    
  }
  
  @Override
  public float getTimeSpent() {
    float timeSpent = 0;
    for (ITutor child: childTutors) {
      timeSpent += child.getTimeSpent();
    }
    return timeSpent;
  }
  
  @Override
  public float getNumAttempts() {
    int numAttempted = 0;
    for (ITutor child: childTutors) {
      if (child.getNumAttempts() > 0) numAttempted++;
    }
    return numAttempted;
  }
  
  @Override
  public float getPercentAttempted() {
    float attemptPercent = 0;
    for (ITutor child: childTutors) {
      attemptPercent += child.getPercentAttempted();
    }
    return attemptPercent / childTutors.size();
  }

  @Override
  public float getNumSuccesses() {
    int numSuccesses = 0;
    for (ITutor child: childTutors) {
      if (child.getNumSuccesses() > 0) numSuccesses++;
    }
    return numSuccesses;
  }
  
}