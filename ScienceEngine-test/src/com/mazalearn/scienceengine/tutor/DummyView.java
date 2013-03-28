package com.mazalearn.scienceengine.tutor;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.core.view.ModelControls;
import com.mazalearn.scienceengine.core.view.ViewControls;

public class DummyView implements IScience2DView {

  @Override
  public void done(boolean success) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void suspend(boolean value) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean isSuspended() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Array<Actor> getActors() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void tutoring(boolean tutoringOn) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean isTutoringInProgress() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Actor findActor(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<List<Actor>> getLocationGroups() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void addLocationGroup(Actor[] actors) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void removeLocationGroups() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void prepareView() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void checkGuruProgress() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public BitmapFont getFont() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<IModelConfig<?>> getCommands() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ModelControls getModelControls() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ViewControls getViewControls() {
    // TODO Auto-generated method stub
    return null;
  }

}
