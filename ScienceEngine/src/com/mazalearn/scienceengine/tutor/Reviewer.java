package com.mazalearn.scienceengine.tutor;

import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.core.controller.IScience2DController;

public class Reviewer extends TutorGroup {

  public Reviewer(IScience2DController science2DController, ITutor parent,
      String goal, String id, Array<?> components, Array<?> configs, 
      int deltaSuccessScore, int deltaFailureScore, String[] hints) {
    super(science2DController, parent, goal, id, components, configs, deltaSuccessScore, deltaFailureScore, hints);
  }
}
