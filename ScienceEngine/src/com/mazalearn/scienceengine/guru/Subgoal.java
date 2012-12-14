package com.mazalearn.scienceengine.guru;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.lang.Expr;
import com.mazalearn.scienceengine.core.lang.IFunction;
import com.mazalearn.scienceengine.core.lang.Parser;
import com.mazalearn.scienceengine.core.lang.SyntaxException;
import com.mazalearn.scienceengine.core.lang.Variable;
import com.mazalearn.scienceengine.core.model.IScience2DModel;

public class Subgoal {
  private final String hint;
  private final Expr postCondition;
  private Collection<Variable> variables;
  private int deltaSuccessScore;
  private String when;
  private boolean progress;

  public Subgoal(String hintText) {
    this(hintText, null, "1", 60);
  }
  
  public Subgoal(String hint, String when, String postConditionString, int deltaSuccessScore) {
    this.hint = hint;
    Parser parser = new Parser();
    Map<String, IFunction> functions = new HashMap<String, IFunction>();
    functions.put("Count", new IFunction() {
      public float eval(String name) { 
        return ScienceEngine.getEventLog().eval("Count", name);
      } 
    });
    parser.allowFunctions(functions);
    try {
      this.postCondition = parser.parseString(postConditionString);
    } catch (SyntaxException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    this.variables = parser.getVariables();
    this.when = when;
    this.deltaSuccessScore = deltaSuccessScore;
  }

  public String getHint(float timeElapsed) {
    if (!progress) {
      return hint;
    }
    return null;
  }
  
  public String getWhen() {
    return when;
  }

  public boolean isStageCompleted(IScience2DModel science2DModel) {
    if (postCondition == null) return false;  
    science2DModel.bindParameterValues(variables);
    return postCondition.bvalue();
  }

  public long getDeltaSuccessScore() {
    return deltaSuccessScore;
  }

  public void checkProgress(IScience2DModel science2DModel) {
    this.progress = isStageCompleted(science2DModel);
  }
}