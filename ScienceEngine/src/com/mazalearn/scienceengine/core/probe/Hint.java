package com.mazalearn.scienceengine.core.probe;

import java.util.Set;

import com.mazalearn.scienceengine.core.lang.Expr;
import com.mazalearn.scienceengine.core.lang.Variable;

public class Hint {
  private final String hintText;
  private final Expr expr;
  private Set<Variable> variables;

  public Hint(String hintText) {
    this(hintText, null, null);
  }
  
  public Hint(String hintText, Expr expr, Set<Variable> variables) {
    this.hintText = hintText;
    this.expr = expr;
    this.variables = variables;
  }

  public String getHintText() {
    return hintText;
  }

  public Expr getExpr() {
    return expr;
  }
  
  public Set<Variable> getVariables() {
    return variables;
  }
}