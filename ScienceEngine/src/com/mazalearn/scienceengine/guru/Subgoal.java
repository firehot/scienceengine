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
import com.mazalearn.scienceengine.core.view.IScience2DView;

public class Subgoal extends AbstractTutor {
  private final String title;
  private final Expr postCondition;
  private Collection<Variable> variables;
  private String when;
  private boolean progress;

  public Subgoal(IScience2DModel science2DModel, IScience2DView science2DView,
      String title, String when, String postConditionString,
      int deltaSuccessScore) {
    super(science2DModel, science2DView, deltaSuccessScore, 0);
    this.title = title;
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
  }

  public String getHint() {
    if (!progress) {
      return title;
    }
    return null;
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
  public String getTitle() {
    return title;
  }

  @Override
  public void activate(boolean activate) {
    // TODO Auto-generated method stub
    
  }
}