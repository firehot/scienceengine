package com.mazalearn.scienceengine.view;

import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Actor;

public interface IExperimentView {
  // Pause model actions in the experiment. Measurement will still work.
  public void pause();
  // Resume model actions
  public void resume();
  // Is the model paused?
  public boolean isPaused();
  // Get all the actors
  public List<Actor> getActors();
  // Ask series of questions to probe learner's knowledge and understanding
  // If successful, learner can move to next level.
  public void challenge(boolean challenge);
  // Whether probe mode is on
  public boolean isChallengeInProgress();
}
