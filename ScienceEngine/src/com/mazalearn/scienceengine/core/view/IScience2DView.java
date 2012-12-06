package com.mazalearn.scienceengine.core.view;

import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.core.guru.AbstractScience2DProber;
import com.mazalearn.scienceengine.core.guru.IDoneCallback;
import com.mazalearn.scienceengine.core.guru.ProbeManager;
import com.mazalearn.scienceengine.core.model.Science2DBody;

public interface IScience2DView extends IDoneCallback {
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
  // Return actor corresponding to name, if exists else null
  public Actor findActor(String name);
  // Return list of location groups
  public List<List<Actor>> getLocationGroups();
  // Add location group consisting of actors
  public void addLocationGroup(Actor[] actors);
  // Remove all location groups
  public void removeLocationGroups();
  // Add an actor with science model behind
  Actor addScience2DActor(Science2DBody science2DBody);
  // Add an actor with only visual behaviour and no model
  Actor addVisualActor(String name);
  // Once all actors are created, prepare the stage
  void prepareStage();
  // Get probe manager for the stage
  public ProbeManager getProbeManager();
  // Create prober
  public AbstractScience2DProber createProber(String proberName, ProbeManager probeManager);
}
