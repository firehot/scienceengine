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
    
  public Guide(IScience2DController science2DController,
      String goal, Array<?> components, Array<?> configs, 
      int deltaSuccessScore, int deltaFailureScore) {
    super(science2DController, goal, components, configs, deltaSuccessScore, deltaFailureScore);
  }
  
  /* (non-Javadoc)
   * @see com.mazalearn.scienceengine.guru.AbstractTutor#activate(boolean)
   */
  @Override
  public void activate(boolean activate) {
    if (activate) {
      stageBeginTime[currentStage] = ScienceEngine.getTime();
    } 
    ScienceEngine.setProbeMode(false);
    this.setVisible(activate);
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
      activateStage(0);
    }
  }
  
  @Override
  public void act(float delta) {
    super.act(delta);
    if (Math.round(ScienceEngine.getTime()) % 2 != 0) return;
    if (currentStage < 0 || currentStage == subgoals.size()) return;
    Subgoal subgoal = subgoals.get(currentStage);
    while (subgoal.hasSucceeded()) {
      subgoal.activate(false);
      currentStage++;
      stageBeginTime[currentStage] = ScienceEngine.getTime();
      science2DController.getGuru().done(true);
      if (currentStage == subgoals.size()) {
        break;
      }
      subgoal = activateStage(currentStage);
    }
  }

  private Subgoal activateStage(int currentStage) {
    this.currentStage = currentStage;
    Subgoal subgoal = subgoals.get(currentStage);
    subgoal.reinitialize(true);
    subgoal.activate(true);
    return subgoal;
  }

  /* (non-Javadoc)
   * @see com.mazalearn.scienceengine.guru.AbstractTutor#getHint()
   */
  @Override
  public String getHint() {
    if (currentStage < 0 || currentStage == subgoals.size()) return null;
    // float timeElapsed = ScienceEngine.getTime() - stageBeginTime[currentStage];
    Subgoal subgoal = subgoals.get(currentStage);
    return subgoal.getGoal();
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
  public boolean hasSucceeded() {
    return currentStage == subgoals.size();
  }
  
  @Override
  public void doSuccessActions() {
    if (successActions == null) return;
    science2DController.getModel().bindParameterValues(variables);
    successActions.bvalue();    
  }

  @Override
  public boolean hasFailed() {
    return false; // Allow learner to keep trying forever
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