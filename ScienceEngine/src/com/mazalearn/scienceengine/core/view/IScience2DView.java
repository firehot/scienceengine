package com.mazalearn.scienceengine.core.view;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.tutor.IDoneCallback;

public interface IScience2DView extends IDoneCallback {
  // Pause/Resume model actions in the activity. 
  // Measurement/sensors will still work when paused
  public void suspend(boolean value);
  // Is the model paused?
  public boolean isSuspended();
  // Get all the actors
  public Array<Actor> getActors();
  // Ask series of questions to probe learner's knowledge and understanding
  // If successful, learner can move to next level.
  public void tutoring(boolean tutoringOn);
  // Whether probe mode is on
  public boolean isTutoringInProgress();
  // Return actor corresponding to name, if exists else null
  public Actor findActor(String name);
  // Return list of location groups
  public List<List<Actor>> getLocationGroups();
  // Add location group consisting of actors
  public void addLocationGroup(Actor[] actors);
  // Remove all location groups
  public void removeLocationGroups();
  // Once all actors are created, prepare the stage
  void prepareView();
  // Check progress of guru
  void checkGuruProgress();
  // return font for this view
  public BitmapFont getFont();
  // Commands available for this view
  List<IModelConfig<?>> getCommands();
  // Get model controls for this view
  public ModelControls getModelControls();
  // Get view controls for this view
  ViewControls getViewControls();
  // show help
  void showHelp();
}
