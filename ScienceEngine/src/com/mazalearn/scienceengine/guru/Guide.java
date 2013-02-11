package com.mazalearn.scienceengine.guru;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.lang.Expr;
import com.mazalearn.scienceengine.core.lang.IFunction;
import com.mazalearn.scienceengine.core.lang.Parser;
import com.mazalearn.scienceengine.core.lang.SyntaxException;
import com.mazalearn.scienceengine.core.lang.Variable;

public class Guide extends AbstractTutor {
  
  private List<Subgoal> subgoals = Collections.emptyList();
  
  private float[] stageBeginTime;
  private int currentStage = -1;

  private Expr successActions;

  private Set<Variable> variables;
    
  public Guide(IScience2DController science2DController, ITutor parent,
      String goal, Array<?> components, Array<?> configs, 
      int deltaSuccessScore, int deltaFailureScore) {
    super(science2DController, parent, goal, components, configs, deltaSuccessScore, deltaFailureScore);
  }
  
  /* (non-Javadoc)
   * @see com.mazalearn.scienceengine.guru.AbstractTutor#activate(boolean)
   */
  @Override
  public void activate(boolean activate) {
    super.activate(activate);
    if (activate) {
      stageBeginTime[currentStage] = ScienceEngine.getTime();
    } 
    ScienceEngine.setProbeMode(false);
  }

  /* (non-Javadoc)
   * @see com.mazalearn.scienceengine.guru.AbstractTutor#reinitialize(boolean)
   */
  /**
   * Guide allows user to interact with bodies on screen as well as with its 
   * own bodies - like subgoal.
   */
  @Override
  public void reinitialize(boolean probeMode) {
    super.reinitialize(probeMode);
    if (probeMode) {
      this.currentStage = 0;
      Subgoal subgoal = subgoals.get(0);
      subgoal.reinitialize(true);
      subgoal.activate(true);
    }
  }
  
  @Override
  public void reset() {
    super.reset();
    if (currentStage < 0 || currentStage == subgoals.size()) return;
    Subgoal subgoal = subgoals.get(currentStage);
    subgoal.reset();
  }
  
  @Override
  public void done(boolean success) {
    if (!success) {
      super.done(success);
      return;
    }
    // Move on to next stage
    if (++currentStage == subgoals.size()) {
      super.done(success);
      return;
    }
    Subgoal subgoal = subgoals.get(currentStage);
    subgoal.reinitialize(true);
    subgoal.activate(true);
  }

  @Override
  public void checkProgress() {
    if (currentStage < 0 || currentStage == subgoals.size()) return;
    Subgoal subgoal = subgoals.get(currentStage);
    subgoal.checkProgress();
  }
  
  public void initialize(List<Subgoal> subgoals, String successActionsString) {
    this.subgoals = subgoals;
    for (Subgoal subgoal: subgoals) {
      this.addActor(subgoal);
      subgoal.activate(false);
    }
    // End timeLimit of stage is begin timeLimit of stage i+1. So we need 1 extra
    this.stageBeginTime = new float[subgoals.size() + 1];
    if (successActionsString != null) {
      Parser parser = createParser();
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
  public void doSuccessActions() {
    if (successActions == null) return;
    science2DController.getModel().bindParameterValues(variables);
    successActions.bvalue();    
  }

  private Parser createParser() {
    Parser parser = new Parser();
    Map<String, IFunction.A0> functions0 = new HashMap<String, IFunction.A0>();
    Map<String, IFunction.A1> functions1 = new HashMap<String, IFunction.A1>();
    Map<String, IFunction.A2> functions2 = new HashMap<String, IFunction.A2>();

    for (final IModelConfig<?> command: science2DController.getView().getCommands()) {
      functions0.put(command.getName(), new IFunction.A0() {
         @Override
         public float eval() { command.doCommand(); return 0; }
      });
    }

    parser.allowFunctions(functions0, functions1, functions2);
    return parser;
  }

}