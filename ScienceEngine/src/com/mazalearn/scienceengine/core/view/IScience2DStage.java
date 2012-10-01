package com.mazalearn.scienceengine.core.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.app.services.LevelManager;
import com.mazalearn.scienceengine.core.probe.IDoneCallback;

public interface IScience2DStage extends IDoneCallback {
  // Pause/Resume model actions in the experiment. 
  // Measurement/sensors will still work when paused
  public void suspend(boolean value);
  // Is the model paused?
  public boolean isSuspended();
  // Get all the actors
  public Array<Actor> getActors();
  // Ask series of questions to probe learner's knowledge and understanding
  // If successful, learner can move to next level.
  public void challenge(boolean challenge);
  // Whether probe mode is on
  public boolean isChallengeInProgress();
  public LevelManager getLevelManager();
}
