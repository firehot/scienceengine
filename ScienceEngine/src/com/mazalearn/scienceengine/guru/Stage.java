package com.mazalearn.scienceengine.guru;

import java.util.Collection;

import com.mazalearn.scienceengine.core.lang.Expr;
import com.mazalearn.scienceengine.core.lang.Parser;
import com.mazalearn.scienceengine.core.lang.SyntaxException;
import com.mazalearn.scienceengine.core.lang.Variable;
import com.mazalearn.scienceengine.core.model.IScience2DModel;

public class Stage {
  private final String hint;
  private final Expr postCondition;
  private Collection<Variable> variables;
  private int timeLimit;

  public Stage(String hintText) {
    this(hintText, "1", 60);
  }
  
  public Stage(String hint, String postConditionString, int timeLimit) {
    this.hint = hint;
    Parser parser = new Parser();
    try {
      this.postCondition = parser.parseString(postConditionString);
    } catch (SyntaxException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    this.variables = parser.getVariables();
    this.timeLimit = timeLimit;
  }

  public String getHint() {
    return hint;
  }

  public boolean isStageCompleted(IScience2DModel science2DModel) {
    if (postCondition == null) return false;  
    science2DModel.bindParameterValues(variables);
    return postCondition.bvalue();
  }

  public long getTimeLimit() {
    return timeLimit;
  }
}