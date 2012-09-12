package com.mazalearn.scienceengine.view;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.model.IExperimentModel;
import com.mazalearn.scienceengine.services.LevelManager;
import com.mazalearn.scienceengine.services.SoundManager;

public abstract class AbstractExperimentView extends Stage implements IExperimentView {

  public static final int PIXELS_PER_M = 8;
  protected final IExperimentModel experimentModel;
  protected final Skin skin;
  protected final SoundManager soundManager;
  private boolean isChallengeInProgress = false;
  private LevelManager levelManager;

  public AbstractExperimentView(String experimentName, 
      IExperimentModel experimentModel, float width, float height, Skin skin, 
      SoundManager soundManager) {
    super(width, height, true);
    this.skin = skin;
    this.soundManager = soundManager;
    this.experimentModel = experimentModel;
    levelManager = new LevelManager(experimentName, this, 
        experimentModel.getConfigs());
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
  
  public LevelManager getLevelManager() {
    return levelManager;
  }
}