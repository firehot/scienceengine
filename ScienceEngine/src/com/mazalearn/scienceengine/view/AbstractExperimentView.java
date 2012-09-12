package com.mazalearn.scienceengine.view;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mazalearn.scienceengine.model.IExperimentModel;

public abstract class AbstractExperimentView extends Stage implements IExperimentView {

  public static final int PIXELS_PER_M = 8;
  protected final IExperimentModel experimentModel;
  private boolean isChallengeInProgress = false;

  public AbstractExperimentView(IExperimentModel experimentModel, float width, float height) {
    super(width, height, true);
    this.experimentModel = experimentModel;
  }

  @Override
  public void pause() {
    experimentModel.enable(false);
  }

  @Override
  public void resume() {
    experimentModel.enable(true);
  }

  @Override
  public boolean isPaused() {
    return !experimentModel.isEnabled();
  }
  
  public void done(boolean success) {}
/*
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
      super.draw(batch, parentAlpha);
      ScienceEngine.debugCamera.unproject(getStage().getCamera().position);
      ScienceEngine.debugRenderer.render(experimentModel.getBox2DWorld(), ScienceEngine.debugCamera.combined);
  }
*/
  
  /**
   * Draw and advance Box2D World
   * 
   */
  @Override
  public void draw() {
    super.draw();
  }

  @Override
  public void challenge(boolean challenge) {
    experimentModel.reset();
    isChallengeInProgress = !isChallengeInProgress;
  }
    
  @Override
  public boolean isChallengeInProgress() {
    return isChallengeInProgress;
  }
  
  @Override
  public void act(float delta) {
    experimentModel.simulateSteps(delta);
    super.act(delta);
  }
}