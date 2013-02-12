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
  
  private List<ITutor> childTutors = Collections.emptyList();
  
  private float[] stageBeginTime;
  private int currentStage = -1;

  private Expr successActions;

  private Set<Variable> variables;
    
  public Guide(IScience2DController science2DController, ITutor parent,
      String goal, Array<?> components, Array<?> configs, 
      int deltaSuccessScore, int deltaFailureScore, String[] hints) {
    super(science2DController, parent, goal, components, configs, deltaSuccessScore, deltaFailureScore, hints);
  }
  
  @Override
  public void reset() {
    super.reset();
    if (currentStage < 0 || currentStage == childTutors.size()) return;
    ITutor subgoal = childTutors.get(currentStage);
    subgoal.reset();
  }
  
  @Override
  public void done(boolean success) {
    if (!success) {
      super.done(success);
      return;
    }
    // Move on to next stage
    if (++currentStage == childTutors.size()) {
      super.done(success);
      doSuccessActions();
      return;
    }
    ITutor childTutor = childTutors.get(currentStage);
    childTutor.prepareToTeach();
    childTutor.teach();
  }

  /* (non-Javadoc)
   * @see com.mazalearn.scienceengine.guru.AbstractTutor#activate(boolean)
   */
  @Override
  public void teach() {
    super.teach();
    stageBeginTime[currentStage] = ScienceEngine.getTime();
    ScienceEngine.setProbeMode(false);
    ITutor childTutor = childTutors.get(0);
    childTutor.teach();
  }

  /* (non-Javadoc)
   * @see com.mazalearn.scienceengine.guru.AbstractTutor#reinitialize(boolean)
   */
  /**
   * Guide allows user to interact with bodies on screen as well as with its 
   * own bodies - like subgoal.
   */
  @Override
  public void prepareToTeach() {
    super.prepareToTeach();
    this.currentStage = 0;
    ITutor childTutor = childTutors.get(0);
    childTutor.prepareToTeach();
  }
  
  @Override
  public void checkProgress() {
    if (currentStage < 0 || currentStage == childTutors.size()) return;
    ITutor childTutor = childTutors.get(currentStage);
    childTutor.checkProgress();
  }
  
  public void initialize(List<ITutor> childTutors, String successActionsString) {
    this.childTutors = childTutors;
    for (ITutor childTutor: childTutors) {
      this.addActor((AbstractTutor) childTutor);
    }
    // End timeLimit of stage is begin timeLimit of stage i+1. So we need 1 extra
    this.stageBeginTime = new float[childTutors.size() + 1];
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

  private void doSuccessActions() {
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