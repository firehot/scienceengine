package com.mazalearn.scienceengine.tutor;

import java.util.Collection;

import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.lang.Expr;
import com.mazalearn.scienceengine.core.lang.Parser;
import com.mazalearn.scienceengine.core.lang.SyntaxException;
import com.mazalearn.scienceengine.core.lang.Variable;

public class KnowledgeUnit extends AbstractTutor {
  private Expr postCondition;
  private Collection<Variable> variables;
  private String when;
  //  private String[] plaudits = {"Bravo", "Well Done", "Excellent", "Good"};
  
  public KnowledgeUnit(IScience2DController science2DController,
      ITutor parent, String goal, String id, Array<?> components, Array<?> configs,
      int deltaSuccessScore, String[] hints) {
    super(science2DController, parent, goal, id, components, configs, deltaSuccessScore, 0, hints);
    
  }

  public void initialize(String when, String postConditionString) {
    Parser parser = guru.createParser();
    try {
      this.postCondition = parser.parseString(postConditionString);
    } catch (SyntaxException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    this.variables = parser.getVariables();
    this.when = when;
  }

  public String getWhen() {
    return when;
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    if (!isVisible() || Math.round(ScienceEngine.getTime()) % 2 != 0) return;
    checkProgress();
  }

  @Override
  public void prepareToTeach(ITutor childTutor) {
    super.prepareToTeach(childTutor);
  }
  
  @Override
  public void checkProgress() {
    if (postCondition == null) return;
    if (!isComplete()) {
      science2DController.getModel().bindParameterValues(variables);
      prepareToFinish(postCondition.bvalue());
    }
  }
}