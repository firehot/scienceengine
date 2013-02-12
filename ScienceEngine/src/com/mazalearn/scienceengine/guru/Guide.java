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
  
  private float[] tutorBeginTime;
  private int tutorIndex = -1;

  private Expr successActions;

  private Set<Variable> variables;

  private ITutor currentTutor;
    
  public Guide(IScience2DController science2DController, ITutor parent,
      String goal, Array<?> components, Array<?> configs, 
      int deltaSuccessScore, int deltaFailureScore, String[] hints) {
    super(science2DController, parent, goal, components, configs, deltaSuccessScore, deltaFailureScore, hints);
  }
  
  @Override
  public void reset() {
    super.reset();
    if (currentTutor != null) {
      currentTutor.reset();
    }
  }
  
  @Override
  public void done(boolean success) {
    if (!success) {
      super.done(success);
      return;
    }
    // Move on to next stage
    if (++tutorIndex == childTutors.size()) {
      super.done(success);
      doSuccessActions();
      return;
    }
    currentTutor = childTutors.get(tutorIndex);
    currentTutor.prepareToTeach();
    currentTutor.teach();
  }

  /* (non-Javadoc)
   * @see com.mazalearn.scienceengine.guru.AbstractTutor#activate(boolean)
   */
  @Override
  public void teach() {
    super.teach();
    tutorBeginTime[tutorIndex] = ScienceEngine.getTime();
    ScienceEngine.setProbeMode(false);
    currentTutor.teach();
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
    this.tutorIndex = 0;
    currentTutor = childTutors.get(0);
    currentTutor.prepareToTeach();
  }
  
  @Override
  public void checkProgress() {
    if (currentTutor == null) return;
    currentTutor.checkProgress();
  }
  
  public void initialize(List<ITutor> childTutors, String successActionsString) {
    this.childTutors = childTutors;
    for (ITutor childTutor: childTutors) {
      this.addActor((AbstractTutor) childTutor);
    }
    // End timeLimit of stage is begin timeLimit of stage i+1. So we need 1 extra
    this.tutorBeginTime = new float[childTutors.size() + 1];
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