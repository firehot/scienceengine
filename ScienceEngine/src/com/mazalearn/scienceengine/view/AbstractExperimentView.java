package com.mazalearn.scienceengine.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.mazalearn.scienceengine.model.IExperimentModel;

public class AbstractExperimentView extends Group implements IExperimentView {

  public static final int PIXELS_PER_M = 8;
  protected boolean isPaused = false;
  protected final IExperimentModel experimentModel;
  protected Map<String, Actor> components;

  public AbstractExperimentView(IExperimentModel experimentModel) {
    super();
    this.experimentModel = experimentModel;
    this.components = new HashMap<String, Actor>();
  }

  @Override
  public void pause() {
    this.isPaused = true;
  }

  @Override
  public void resume() {
    this.isPaused = false;
  }

  @Override
  public boolean isPaused() {
    return isPaused;
  }
  
/*
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
      super.draw(batch, parentAlpha);
      ScienceEngine.debugCamera.unproject(getStage().getCamera().position);
      ScienceEngine.debugRenderer.render(experimentModel.getBox2DWorld(), ScienceEngine.debugCamera.combined);
  }
*/
  
  @Override
  public List<Actor> getActors() {
    return super.getActors();
  }
}