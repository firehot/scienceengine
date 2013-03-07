package com.mazalearn.scienceengine.app.services.loaders;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.tutor.AbstractTutor;
import com.mazalearn.scienceengine.tutor.Abstractor;
import com.mazalearn.scienceengine.tutor.ITutor;
import com.mazalearn.scienceengine.tutor.ITutor.Type;
import com.mazalearn.scienceengine.tutor.KnowledgeUnit;
import com.mazalearn.scienceengine.tutor.McqTutor;
import com.mazalearn.scienceengine.tutor.ParameterProber;
import com.mazalearn.scienceengine.tutor.TutorGroup;

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
    float successPoints = (Float) LevelLoader.nvl(tutorObj.get("success"),
        100.0f);
    float failurePoints = (Float) LevelLoader.nvl(tutorObj.get("falure"),
        50.0f);
    Array<?> components = (Array<?>) tutorObj.get("components");
    Array<?> configs = (Array<?>) tutorObj.get("configs");
    String[] hints = loadStringArray("hints", tutorObj);
    AbstractTutor tutor = science2DController.createTutor(parentTutor, type, goal, id,
        components, configs, (int) successPoints, (int) failurePoints, hints);
    switch (tutor.getType()) {
    case MCQ1:
    case MCQ: return makeMcqTutor(tutorObj, (McqTutor) tutor);
    case ParameterProber: return makeParameterProber(tutorObj, (ParameterProber) tutor);
    case Reviewer: return makeReviewer(tutorObj, (TutorGroup) tutor);
    case Challenge:
    case RapidFire:
    case Guide: return makeTutorGroup(tutorObj, (TutorGroup) tutor);
    case Abstractor: return makeAbstractor(tutorObj, (Abstractor) tutor);
    case KnowledgeUnit: return makeKnowledgeUnit(tutorObj, (KnowledgeUnit) tutor);
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
    String explanation = (String) tutorObj.get("explanation");
    String[] options = loadStringArray("options", tutorObj);
    mcqTutor.initialize(options, explanation, answerMask);
    return mcqTutor;
  }

  private AbstractTutor makeTutorGroup(OrderedMap<String, ?> tutorObj, TutorGroup tutorGroup) {
    Array<?> childTutorsObj = (Array<?>) tutorObj.get("childtutors");
    List<ITutor> childTutors = loadChildTutors(tutorGroup, childTutorsObj);
    String successActions = (String) tutorObj.get("successactions");
    tutorGroup.initialize(childTutors, successActions);
    return tutorGroup;
  }

  @SuppressWarnings("unchecked")
  private AbstractTutor makeReviewer(OrderedMap<String, ?> tutorObj, TutorGroup reviewer) {
    Gdx.app.log(ScienceEngine.LOG, "Loading Reviewer");
    List<ITutor> reviewTutors = new ArrayList<ITutor>();
    // Assumption: Current level is the review level
    for (int level = 1; level < science2DController.getLevel(); level++) {
      OrderedMap<String, ?> rootElem = LevelLoader.getJsonFromFile(science2DController.getTopic(), level);
      Array<?> tutorsObj = (Array<?>) rootElem.get("tutors");
      for (int i = 0; i < tutorsObj.size; i++) {
        OrderedMap<String, ?> groupTutorsObj = (OrderedMap<String, ?>) tutorsObj.get(i);
        if (Type.RapidFire.name().equals(groupTutorsObj.get("type"))) {
          Array<?> childTutorsObj = (Array<?>) groupTutorsObj.get("childtutors");
          List<ITutor> childTutors = loadChildTutors(reviewer, childTutorsObj);
          reviewTutors.addAll(childTutors);
        }
      }
    }
    String successActions = (String) tutorObj.get("successactions");
    reviewer.initialize(reviewTutors, successActions);
    return reviewer;
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
      Gdx.app.error(ScienceEngine.LOG, "No child Tutors found for Tutor: " + parent.getGoal());
      return childTutors;
    }
    for (int i = 0; i < childTutorsObj.size; i++) {
      childTutors.add(loadTutor(parent, (OrderedMap<String, ?>) childTutorsObj.get(i)));
    }
    return childTutors;
  }
}