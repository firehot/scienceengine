package com.mazalearn.scienceengine.guru;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.Function;
import com.mazalearn.scienceengine.core.lang.Expr;
import com.mazalearn.scienceengine.core.lang.IFunction;
import com.mazalearn.scienceengine.core.lang.Parser;
import com.mazalearn.scienceengine.core.lang.SyntaxException;
import com.mazalearn.scienceengine.core.lang.Variable;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.IScience2DView;

public class Subgoal extends AbstractTutor {
  private final Expr postCondition;
  private Collection<Variable> variables;
  private String when;
  private boolean progress;

  public Subgoal(IScience2DModel science2DModel, IScience2DView science2DView,
      String goal, String when, String postConditionString,
      int deltaSuccessScore) {
    super(science2DModel, science2DView, goal, deltaSuccessScore, 0);
    Parser parser = new Parser();
    Map<String, IFunction> functions = new HashMap<String, IFunction>();
    functions.put("Count", Function.Count); 
    functions.put("Min", Function.Min);
    functions.put("Max", Function.Max);
    parser.allowFunctions(functions);
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

  public boolean isCompleted() {
    if (postCondition == null) return false;  
    science2DModel.bindParameterValues(variables);
    return postCondition.bvalue();
  }

  public void checkProgress() {
    this.progress = isCompleted();
  }

  @Override
  public String getGoal() {
    if (!progress) {
      return super.getGoal();
    }
    return null;
  }

  @Override
  public void activate(boolean activate) {
    // TODO Auto-generated method stub
    
  }
}