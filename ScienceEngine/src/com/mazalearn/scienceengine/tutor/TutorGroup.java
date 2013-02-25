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
  
  private float[] tutorBeginTime;
  private int tutorIndex = -1;

  private Expr successActions;

  private Set<Variable> variables;

  private ITutor currentTutor;

    
  public TutorGroup(IScience2DController science2DController, ITutor parent,
      String goal, String id, Array<?> components, Array<?> configs, 
      int deltaSuccessScore, int deltaFailureScore, String[] hints) {
    super(science2DController, parent, goal, id, components, configs, deltaSuccessScore, deltaFailureScore, hints);
  }
  
  @Override
  public void finish() {
    if (!isComplete) {
      super.finish();
      return;
    }
    // Move on to next stage
    if (++tutorIndex == childTutors.size()) {
      if (getCompletionPercent() == 100) {
        super.finish();
        doSuccessActions();
        return;
      }
      // Goto first tutor which has not been successfully done
      for (tutorIndex = 0; tutorIndex < childTutors.size(); tutorIndex++) {
        if (childTutors.get(tutorIndex).getCompletionPercent() != 100) {
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
    if (currentTutor.getGroupType() == GroupType.Challenge) {
      guru.doChallengeAnimation(currentTutor);
    } else if (currentTutor.getGroupType() == GroupType.RapidFire) {
      guru.doRapidFireAnimation(currentTutor);
    } else {
      currentTutor.teach();
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
        if (childTutors.get(tutorIndex).getCompletionPercent() < 100) break;
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
  public float getCompletionPercent() {
    int numCompletions = 0;
    for (ITutor child: childTutors) {
      if (child.getCompletionPercent() == 100) numCompletions++;
    }
    return numCompletions * 100 / (float) childTutors.size();
  }
}