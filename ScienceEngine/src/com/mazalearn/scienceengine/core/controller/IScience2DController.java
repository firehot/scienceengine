package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.core.view.ModelControls;
import com.mazalearn.scienceengine.core.view.ViewControls;
import com.mazalearn.scienceengine.tutor.AbstractTutor;
import com.mazalearn.scienceengine.tutor.Guru;
import com.mazalearn.scienceengine.tutor.ITutor;

public interface IScience2DController {
  public IScience2DView getView();
  public IScience2DModel getModel();
  public ModelControls getModelControls();
  public Topic getTopic();
  public Topic getLevel();
  /**
   * Reloads the level, reinitializing all components and configurations
   */
  void reset();
  // Factory method to create an actor along with its backing model body
  public Actor addScience2DActor(String type, String viewSpec, float x,
      float y, float rotation);
  public Guru getGuru();
  // Factory method to create tutor
  AbstractTutor createTutor(ITutor parent, String type, Topic topic, String goal, String name, Array<?> components,
      Array<?> configs, String[] hints, String[] explanation, String[] refs);
  // Skin used in this invocation
  Skin getSkin();
  // Return view controls
  public ViewControls getViewControls();
  // Get title of current activity
  String getTitle();
}
