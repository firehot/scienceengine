package com.mazalearn.scienceengine.app.services.loaders;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.guru.AbstractTutor;
import com.mazalearn.scienceengine.guru.Abstractor;
import com.mazalearn.scienceengine.guru.McqTutor;
import com.mazalearn.scienceengine.guru.TutorGroup;
import com.mazalearn.scienceengine.guru.ITutor;
import com.mazalearn.scienceengine.guru.ParameterProber;
import com.mazalearn.scienceengine.guru.KnowledgeUnit;

class TutorLoader {

  private IScience2DController science2DController;
  
  public TutorLoader(IScience2DController science2DController) {
    this.science2DController = science2DController;
  }

  private AbstractTutor loadTutor(ITutor parentTutor, OrderedMap<String, ?> tutorObj) {
    String type = (String) tutorObj.get("type");
    String id = (String) tutorObj.get("id");
    Gdx.app.log(ScienceEngine.LOG, "Loading tutor: " + type);
    String goal = (String) tutorObj.get("goal");
    float deltaSuccessScore = (Float) LevelLoader.nvl(tutorObj.get("success"),
        100.0f);
    float deltaFailureScore = (Float) LevelLoader.nvl(tutorObj.get("falure"),
        50.0f);
    Array<?> components = (Array<?>) tutorObj.get("components");
    Array<?> configs = (Array<?>) tutorObj.get("configs");
    String[] hints = loadStringArray("hints", tutorObj);
    AbstractTutor tutor = science2DController.createTutor(parentTutor, type, goal, id,
        components, configs, (int) deltaSuccessScore, (int) deltaFailureScore, hints);
    if (tutor instanceof McqTutor) {
      return makeMcqTutor(tutorObj, (McqTutor) tutor);
    }
    if (tutor instanceof ParameterProber) {
      return makeParameterProber(tutorObj, (ParameterProber) tutor);
    }
    if (tutor instanceof TutorGroup) {
      return makeTutorGroup(tutorObj, (TutorGroup) tutor);
    }
    if (tutor instanceof Abstractor) {
      return makeAbstractor(tutorObj, (Abstractor) tutor);
    }
    if (tutor instanceof KnowledgeUnit) {
      return makeKnowledgeUnit(tutorObj, (KnowledgeUnit) tutor);
    }
    return tutor;
  }

  private AbstractTutor makeParameterProber(OrderedMap<String, ?> tutorObj,
      ParameterProber parameterProber) {
    String resultType = (String) tutorObj.get("resultType");
    String parameterName = (String) tutorObj.get("parameter");
    String resultExpr = (String) tutorObj.get("result");
    IModelConfig<?> parameter = science2DController.getModel().getConfig(parameterName);
    parameterProber.initialize(parameter, resultExpr, resultType);
    return parameterProber;
  }

  private AbstractTutor makeMcqTutor(OrderedMap<String, ?> tutorObj,
      McqTutor mcqTutor) {
    String answerMask = (String) tutorObj.get("answermask");
    String[] options = loadStringArray("options", tutorObj);
    mcqTutor.initialize(options, answerMask);
    return mcqTutor;
  }

  private AbstractTutor makeTutorGroup(OrderedMap<String, ?> tutorObj, TutorGroup tutorGroup) {
    String groupType = (String) tutorObj.get("group");
    Array<?> childTutorsObj = (Array<?>) tutorObj.get("childtutors");
    List<ITutor> childTutors = loadChildTutors(tutorGroup, childTutorsObj);
    String successActions = (String) tutorObj.get("successactions");
    tutorGroup.initialize(groupType, childTutors, successActions);
    return tutorGroup;
  }

  private AbstractTutor makeAbstractor(OrderedMap<String, ?> tutorObj,
      Abstractor abstractor) {
    Array<?> parametersObj = (Array<?>) tutorObj.get("parameters");
    String[] parameters = parametersObj == null ? new String[]{} : new String[parametersObj.toArray().length];
    for (int i = 0; i < parameters.length; i++) {
      parameters[i] = (String) parametersObj.get(i);
    }
    abstractor.initialize(parameters);
    return abstractor;
  }

  private KnowledgeUnit makeKnowledgeUnit(OrderedMap<String, ?> tutorObj, KnowledgeUnit knowledgeUnit) {
    String when = (String) tutorObj.get("when");
    String postConditionString = (String) tutorObj.get("postcondition");
    knowledgeUnit.initialize(when, postConditionString);
    return knowledgeUnit;
  }

  private String[] loadStringArray(String attribute, OrderedMap<String, ?> tutorObj) {
    Array<?> hintObj = (Array<?>) tutorObj.get(attribute);
    String[] hints = hintObj == null ? new String[]{} : new String[hintObj.toArray().length];
    for (int i = 0; i < hints.length; i++) {
      hints[i] = (String) hintObj.get(i);
    }
    return hints;
  }

  @SuppressWarnings("unchecked") 
  public List<ITutor> loadChildTutors(ITutor parent, Array<?> childTutorsObj) {
    List<ITutor> childTutors = new ArrayList<ITutor>();
    if (childTutorsObj == null) {
      Gdx.app.error(ScienceEngine.LOG, "No child Tutors found for Tutor");
      return childTutors;
    }
    for (int i = 0; i < childTutorsObj.size; i++) {
      childTutors.add(loadTutor(parent, (OrderedMap<String, ?>) childTutorsObj.get(i)));
    }
    return childTutors;
  }
}