package com.mazalearn.scienceengine.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.model.IExperimentModel;

public class AbstractExperimentView extends Group implements IExperimentView {

  public static final int PIXELS_PER_M = 8;
  protected boolean isPaused = false;
  protected final IExperimentModel experimentModel;

  public AbstractExperimentView(IExperimentModel experimentModel) {
    super();
    this.experimentModel = experimentModel;
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
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    if (ScienceEngine.DEV_MODE != ScienceEngine.DevMode.BOX2D_DEBUG) {
      super.draw(batch, parentAlpha);
    } else {
      ScienceEngine.debugCamera.unproject(getStage().getCamera().position);
      ScienceEngine.debugRenderer.render(experimentModel.getBox2DWorld(), ScienceEngine.debugCamera.combined);
    }
  }
}