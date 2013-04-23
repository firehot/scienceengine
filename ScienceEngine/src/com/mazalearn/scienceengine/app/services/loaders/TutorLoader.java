package com.mazalearn.scienceengine.app.services.loaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.tutor.AbstractTutor;
import com.mazalearn.scienceengine.tutor.Abstractor;
import com.mazalearn.scienceengine.tutor.ITutor;
import com.mazalearn.scienceengine.tutor.KnowledgeUnit;
import com.mazalearn.scienceengine.tutor.McqTutor;
import com.mazalearn.scienceengine.tutor.ParameterProber;
import com.mazalearn.scienceengine.tutor.TutorGroup;
import com.mazalearn.scienceengine.tutor.TutorType;

class TutorLoader {

  private IScience2DController science2DController;
  
  public TutorLoader(IScience2DController science2DController) {
    this.science2DController = science2DController;
  }

  private AbstractTutor loadTutor(Topic level, ITutor parentTutor, OrderedMap<String, ?> tutorObj) {
    String type = (String) tutorObj.get("type");
    // TutorId is the levelId concatenated with local id of tutor within the level.
    String id = (String) tutorObj.get("id");
    Gdx.app.log(ScienceEngine.LOG, "Loading tutor: " + type + " " + id);
    String goal = (String) tutorObj.get("goal");
    Array<?> components = (Array<?>) tutorObj.get("components");
    Array<?> configs = (Array<?>) tutorObj.get("configs");
    String[] hints = loadStringArray("hints", tutorObj);
    String[] explanation = loadStringArray("explanation", tutorObj);
    String[] refs = loadStringArray("refs", tutorObj);
    AbstractTutor tutor = science2DController.createTutor(parentTutor, type, level, goal, id,
        components, configs, hints, explanation, refs);
    if (tutor == null) return null;
    
    if (tutor.getType() instanceof TutorType) {
      TutorType tutorType = (TutorType) tutor.getType();
      switch (tutorType) {
      case MCQ1:
      case MCQ: return makeMcqTutor(tutorObj, (McqTutor) tutor);
      case ParameterProber: return makeParameterProber(tutorObj, (ParameterProber) tutor);
      case Reviewer: return makeReviewer(tutorObj, (TutorGroup) tutor);
      case Challenge:
      case RapidFire:
      case Guide: return makeTutorGroup(tutorObj, (TutorGroup) tutor);
      case Abstractor: return makeAbstractor(tutorObj, (Abstractor) tutor);
      case KnowledgeUnit: return makeKnowledgeUnit(tutorObj, (KnowledgeUnit) tutor);
      default:
        throw new IllegalArgumentException("Unknown type of tutor: " + tutorType);
      }
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
    String questionImageTexture = (String) tutorObj.get("image");
    String answerMask = (String) tutorObj.get("answermask");
    String[] options = loadStringArray("options", tutorObj);
    mcqTutor.initialize(questionImageTexture, options, answerMask);
    return mcqTutor;
  }

  private AbstractTutor makeTutorGroup(OrderedMap<String, ?> tutorObj, TutorGroup tutorGroup) {
    Array<?> childTutorsObj = (Array<?>) tutorObj.get("childtutors");
    List<ITutor> childTutors = 
        loadChildTutors(science2DController.getLevel(), tutorGroup, childTutorsObj);
    String successActions = (String) tutorObj.get("successactions");
    tutorGroup.initialize(childTutors, successActions);
    return tutorGroup;
  }

  @SuppressWarnings("unchecked")
  private AbstractTutor makeReviewer(OrderedMap<String, ?> tutorObj, TutorGroup reviewer) {
    Gdx.app.log(ScienceEngine.LOG, "Loading Reviewer");
    List<ITutor> reviewTutors = new ArrayList<ITutor>();
    // Assumption: Current level is the review level
    for (Topic level: science2DController.getTopic().getChildren()) {
      if (level == science2DController.getLevel()) break;
      OrderedMap<String, ?> rootElem = LevelLoader.getJsonFromFile(science2DController.getTopic(), level);
      Array<?> tutorsObj = (Array<?>) rootElem.get("tutors");
      for (int i = 0; i < tutorsObj.size; i++) {
        OrderedMap<String, ?> groupTutorsObj = (OrderedMap<String, ?>) tutorsObj.get(i);
        if (TutorType.RapidFire.name().equals(groupTutorsObj.get("type"))) {
          Array<?> childTutorsObj = (Array<?>) groupTutorsObj.get("childtutors");
          List<ITutor> childTutors = loadChildTutors(level, reviewer, childTutorsObj);
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
    Map<String, Integer> parameters = new HashMap<String, Integer>();
    if (parametersObj != null) {
      for (int i = 0; i < parametersObj.toArray().length; i++) {
        String parameter = (String) parametersObj.get(i);
        if (parameter.charAt(0) == '-') {
          parameters.put(parameter.substring(1), -1);
        } else if (parameter.charAt(0) == '+') {
          parameters.put(parameter.substring(1), 1);
        } else {
          parameters.put(parameter, 1); // default is increase
        }
      }
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
    String[] hints = hintObj == null ? new String[0] : new String[hintObj.toArray().length];
    for (int i = 0; i < hints.length; i++) {
      hints[i] = (String) hintObj.get(i);
    }
    return hints;
  }

  @SuppressWarnings("unchecked") 
  public List<ITutor> loadChildTutors(Topic level, ITutor parent, Array<?> childTutorsObj) {
    Gdx.app.log(ScienceEngine.LOG, "Loading child Tutors: " + parent.getId());
    List<ITutor> childTutors = new ArrayList<ITutor>();
    if (childTutorsObj == null) {
      Gdx.app.error(ScienceEngine.LOG, "No child Tutors found for Tutor: " + parent.getId());
      return childTutors;
    }
    for (int i = 0; i < childTutorsObj.size; i++) {
      AbstractTutor tutor = loadTutor(level, parent, (OrderedMap<String, ?>) childTutorsObj.get(i));
      if (tutor != null) {
        childTutors.add(tutor);
      }
    }
    return childTutors;
  }
}