package com.mazalearn.scienceengine.core.guru;

import java.util.Set;

import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.lang.Expr;
import com.mazalearn.scienceengine.core.lang.Variable;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.domains.electromagnetism.model.ComponentType;

public class Stage {
  private final String hint;
  private final Expr postCondition;
  private Set<Variable> variables;
  private int timeLimit;

  public Stage(String hintText) {
    this(hintText, null, 60, null);
  }
  
  public Stage(String hint, Expr postCondition, int timeLimit, Set<Variable> variables) {
    this.hint = hint;
    this.postCondition = postCondition;
    this.timeLimit = timeLimit;
    this.variables = variables;
  }

  public String getHint() {
    return hint;
  }

  @SuppressWarnings("unchecked")
  public boolean isStageCompleted(IScience2DModel science2DModel) {
    if (postCondition == null) return false;
    
    for (Variable v: variables) {
      String name = v.name();
      IModelConfig<?> config = science2DModel.getConfig(name);
      if (config != null) {
        switch (config.getType()) {
        case RANGE: 
          v.setValue(((IModelConfig<Float>) config).getValue()); break;
        case LIST:
        case TEXT:
          v.setValue(((IModelConfig<String>) config).getValue()); break;
        case TOGGLE:
          boolean b = ((IModelConfig<Boolean>) config).getValue();
          v.setValue(b ? 1 : 0);
          break;
        default:
          throw new IllegalStateException("Unexpected config type in expression");
        }
      } else if (name.lastIndexOf(".") != -1) {
        int pos = name.lastIndexOf(".");
        String componentName = name.substring(0, pos);
        String property = name.substring(pos);
        Science2DBody body = science2DModel.findBody(ComponentType.valueOf(componentName));
        if (body != null) {
          if (property.equals("AngularVelocity")) {
            v.setValue(body.getAngularVelocity());
          } else if (property.equals("Angle")) {
            v.setValue(body.getAngle());
          }
        }
      }
    }
    return postCondition.fvalue() == 1;
  }

  public long getTimeLimit() {
    return timeLimit;
  }
}