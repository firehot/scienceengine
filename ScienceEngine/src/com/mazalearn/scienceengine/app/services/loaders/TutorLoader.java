package com.mazalearn.scienceengine.app.services.loaders;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.lang.SyntaxException;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.guru.AbstractTutor;
import com.mazalearn.scienceengine.guru.Guide;
import com.mazalearn.scienceengine.guru.ParameterProber;
import com.mazalearn.scienceengine.guru.Subgoal;

class TutorLoader {

  private IScience2DView science2DView;
  private IScience2DModel science2DModel;

  public TutorLoader(IScience2DModel science2DModel,
      IScience2DView science2DView) {
    this.science2DModel = science2DModel;
    this.science2DView = science2DView;
  }

  public AbstractTutor loadTutor(OrderedMap<String, ?> tutorObj) {
    String name = (String) tutorObj.get("name");
    Gdx.app.log(ScienceEngine.LOG, "Loading tutor: " + name);
    String goal = (String) tutorObj.get("goal");
    String type = (String) tutorObj.get("type");
    float deltaSuccessScore = (Float) LevelLoader.nvl(tutorObj.get("success"),
        100.0f);
    float deltaFailureScore = (Float) LevelLoader.nvl(tutorObj.get("falure"),
        50.0f);
    AbstractTutor tutor = science2DView.createTutor(name, type,
        (int) deltaSuccessScore, (int) deltaFailureScore);
    Array<?> components = (Array<?>) tutorObj.get("components");
    Array<?> configs = (Array<?>) tutorObj.get("configs");
    if (tutor instanceof ParameterProber) {
      String parameterName = (String) tutorObj.get("parameter");
      String resultExpr = (String) tutorObj.get("result");
      Array<?> hintObj = (Array<?>) tutorObj.get("hints");
      String[] hints = new String[hintObj.toArray().length];
      for (int i = 0; i < hints.length; i++) {
        hints[i] = (String) hintObj.get(i);
      }
      IModelConfig<?> parameter = science2DModel.getConfig(parameterName);
      ((ParameterProber) tutor).initialize(goal, parameter, resultExpr, type,
          components, configs, hints);
      return tutor;
    }
    if (tutor instanceof Guide) {
      Array<?> subgoalsObj = (Array<?>) tutorObj.get("subgoals");
      List<Subgoal> subgoals = loadSubgoals(subgoalsObj);
      ((Guide) tutor).initialize(goal, components, configs, subgoals);
      return tutor;
    }
    tutor.initialize(components, configs);
    return tutor;
  }

  @SuppressWarnings("unchecked")
  private List<Subgoal> loadSubgoals(Array<?> subgoalsObj) {
    List<Subgoal> subgoals = new ArrayList<Subgoal>();
    if (subgoalsObj == null)
      return subgoals;
    for (int i = 0; i < subgoalsObj.size; i++) {
      try {
        subgoals.add(loadSubgoal((OrderedMap<String, ?>) subgoalsObj.get(i)));
      } catch (SyntaxException e) {
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
    Subgoal subgoal = new Subgoal(science2DModel, science2DView, goal, when, postCondition, (int) success);
    Array<?> components = (Array<?>) subgoalObj.get("components");
    Array<?> configs = (Array<?>) subgoalObj.get("configs");
    subgoal.initialize(components, configs);
    return subgoal;
  }
}