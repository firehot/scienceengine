package com.mazalearn.scienceengine.app.services.loaders;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.lang.SyntaxException;
import com.mazalearn.scienceengine.guru.AbstractTutor;
import com.mazalearn.scienceengine.guru.Abstractor;
import com.mazalearn.scienceengine.guru.Guide;
import com.mazalearn.scienceengine.guru.ParameterProber;
import com.mazalearn.scienceengine.guru.Subgoal;

class TutorLoader {

  private IScience2DController science2DController;

  public TutorLoader(IScience2DController science2DController) {
    this.science2DController = science2DController;
  }

  public AbstractTutor loadTutor(OrderedMap<String, ?> tutorObj) {
    String type = (String) tutorObj.get("type");
    Gdx.app.log(ScienceEngine.LOG, "Loading tutor: " + type);
    String goal = (String) tutorObj.get("goal");
    float deltaSuccessScore = (Float) LevelLoader.nvl(tutorObj.get("success"),
        100.0f);
    float deltaFailureScore = (Float) LevelLoader.nvl(tutorObj.get("falure"),
        50.0f);
    Array<?> components = (Array<?>) tutorObj.get("components");
    Array<?> configs = (Array<?>) tutorObj.get("configs");
    AbstractTutor tutor = science2DController.createTutor(type, goal,
        components, configs, (int) deltaSuccessScore, (int) deltaFailureScore);
    if (tutor instanceof ParameterProber) {
      String resultType = (String) tutorObj.get("resultType");
      String parameterName = (String) tutorObj.get("parameter");
      String resultExpr = (String) tutorObj.get("result");
      Array<?> hintObj = (Array<?>) tutorObj.get("hints");
      String[] hints = hintObj == null ? new String[]{} : new String[hintObj.toArray().length];
      for (int i = 0; i < hints.length; i++) {
        hints[i] = (String) hintObj.get(i);
      }
      IModelConfig<?> parameter = science2DController.getModel().getConfig(parameterName);
      ((ParameterProber) tutor).initialize(parameter, resultExpr, resultType, hints);
      return tutor;
    }
    if (tutor instanceof Guide) {
      Array<?> subgoalsObj = (Array<?>) tutorObj.get("subgoals");
      List<Subgoal> subgoals = loadSubgoals(subgoalsObj);
      String successActions = (String) tutorObj.get("successactions");
      ((Guide) tutor).initialize(subgoals, successActions);
      return tutor;
    }
    if (tutor instanceof Abstractor) {
      Array<?> parametersObj = (Array<?>) tutorObj.get("parameters");
      String[] parameters = parametersObj == null ? new String[]{} : new String[parametersObj.toArray().length];
      for (int i = 0; i < parameters.length; i++) {
        parameters[i] = (String) parametersObj.get(i);
      }
      ((Abstractor) tutor).initialize(parameters);      
    }
    return tutor;
  }

  @SuppressWarnings("unchecked")
  private List<Subgoal> loadSubgoals(Array<?> subgoalsObj) {
    List<Subgoal> subgoals = new ArrayList<Subgoal>();
    if (subgoalsObj == null) {
      Gdx.app.error(ScienceEngine.LOG, "No subgoals found for Tutor");
      return subgoals;
    }
    for (int i = 0; i < subgoalsObj.size; i++) {
      try {
        subgoals.add(loadSubgoal((OrderedMap<String, ?>) subgoalsObj.get(i)));
      } catch (SyntaxException e) {
        Gdx.app.error(ScienceEngine.LOG, "Could not load subgoal");
        e.printStackTrace();
      }
    }
    return subgoals;
  }

  private Subgoal loadSubgoal(OrderedMap<String, ?> subgoalObj)
      throws SyntaxException {
    String goal = (String) subgoalObj.get("goal");
    String when = (String) subgoalObj.get("when");
    String postCondition = (String) subgoalObj.get("postcondition");
    float success = (Float) LevelLoader.nvl(subgoalObj.get("success"), 100.0f);
    Array<?> components = (Array<?>) subgoalObj.get("components");
    Array<?> configs = (Array<?>) subgoalObj.get("configs");
    Subgoal subgoal = new Subgoal(science2DController, goal, 
        components, configs, when, postCondition, (int) success);
    return subgoal;
  }
}