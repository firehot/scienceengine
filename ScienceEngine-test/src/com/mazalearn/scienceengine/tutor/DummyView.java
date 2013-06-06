package com.mazalearn.scienceengine.tutor;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.core.view.ModelControls;
import com.mazalearn.scienceengine.core.view.ViewControls;

public class DummyView extends Stage implements IScience2DView {

  @Override
  public void done(boolean success) {
    
  }

  @Override
  public void suspend(boolean value) {
    
  }

  @Override
  public boolean isSuspended() {
    return false;
  }

  @Override
  public Array<Actor> getActors() {
    return null;
  }

  @Override
  public void tutoring(boolean tutoringOn) {
    
  }

  @Override
  public boolean isTutoringInProgress() {
    return false;
  }

  @Override
  public Actor findActor(String name) {
    return null;
  }

  @Override
  public List<List<Actor>> getLocationGroups() {
    return null;
  }

  @Override
  public void addLocationGroup(Actor[] actors) {
    
  }

  @Override
  public void removeLocationGroups() {
    
  }

  @Override
  public void prepareView() {
    
  }

  @Override
  public void checkActiveTutorProgress() {
    
  }

  @Override
  public BitmapFont getFont() {
    return null;
  }

  @Override
  public List<IModelConfig<?>> getCommands() {
    return null;
  }

  @Override
  public ModelControls getModelControls() {
    return null;
  }

  @Override
  public ViewControls getViewControls() {
    return null;
  }

}
