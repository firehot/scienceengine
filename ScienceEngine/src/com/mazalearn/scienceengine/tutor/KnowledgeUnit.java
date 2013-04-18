package com.mazalearn.scienceengine.tutor;

import java.util.Collection;

import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.lang.Expr;
import com.mazalearn.scienceengine.core.lang.Parser;
import com.mazalearn.scienceengine.core.lang.SyntaxException;
import com.mazalearn.scienceengine.core.lang.Variable;

public class KnowledgeUnit extends AbstractTutor {
  private Expr postCondition;
  private Collection<Variable> variables;
  private String when;
  
  public KnowledgeUnit(IScience2DController science2DController,
      TutorType tutorType, ITutor parent, String goal, String id, Array<?> components, Array<?> configs,
      String[] hints, String[] explanation, String[] refs) {
    super(science2DController, tutorType, parent, goal, id, components, configs, 
        hints, explanation, refs);
    
  }

  public void initialize(String when, String postConditionString) {
    if (postConditionString == null) {
      postCondition = null;
      variables = null;
      when = null;
      return;
    }
    Parser parser = tutorHelper.createParser();
    try {
      this.postCondition = parser.parseString(postConditionString);
    } catch (SyntaxException e) {
      if (ScienceEngine.DEV_MODE == DevMode.DEBUG) e.printStackTrace();
      throw new RuntimeException(e);
    }
    this.variables = parser.getVariables();
    this.when = when;
  }

  public String getWhen() {
    return when;
  }

  @Override
  public void checkProgress() {
    if (postCondition == null) return;
    if (!isSuccess()) {
      science2DController.getModel().bindParameterValues(variables);
      if (postCondition.bvalue()) {
        systemReadyToFinish(true);
      }
    }
  }
}