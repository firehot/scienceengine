package com.mazalearn.scienceengine.tutor;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.core.view.ModelControls;
import com.mazalearn.scienceengine.core.view.ViewControls;

public class DummyController implements IScience2DController {
  
  private IScience2DView science2DView;
  private Guru guru;
  private Skin skin;

  public DummyController(Skin skin) {
    this.science2DView = new DummyView();
    this.skin = skin;
  }
  
  @Override
  public IScience2DView getView() {
    return science2DView;
  }

  @Override
  public IScience2DModel getModel() {
    return null;
  }

  @Override
  public ModelControls getModelControls() {
    return null;
  }

  @Override
  public Topic getTopic() {
    return null;
  }

  @Override
  public Topic getLevel() {
    return null;
  }

  @Override
  public void reset() {
  }

  @Override
  public Actor addScience2DActor(String type, String viewSpec, float x,
      float y, float rotation) {
    return null;
  }

  @Override
  public Guru getGuru() {
    if (guru == null) {
      guru = new Guru(skin, this, this.getTitle());
    }
    return guru;
  }
  
  @Override
  public AbstractTutor createTutor(ITutor parent, String type, Topic topic, String goal,
      String name, Array<?> components, Array<?> configs,
      String[] hints, String[] explanation,
      String[] refs) {
    return null;
  }

  @Override
  public Skin getSkin() {
    return null;
  }

  @Override
  public ViewControls getViewControls() {
    return null;
  }

  @Override
  public String getTitle() {
    return null;
  }
}