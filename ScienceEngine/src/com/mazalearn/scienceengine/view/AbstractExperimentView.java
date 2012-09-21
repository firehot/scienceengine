package com.mazalearn.scienceengine.view;

import java.util.List;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.controller.Configurator;
import com.mazalearn.scienceengine.controller.IModelConfig;
import com.mazalearn.scienceengine.model.IExperimentModel;
import com.mazalearn.scienceengine.services.LevelManager;
import com.mazalearn.scienceengine.services.SoundManager;

public abstract class AbstractExperimentView extends Stage implements IExperimentView {

  protected final IExperimentModel experimentModel;
  protected final Skin skin;
  protected final SoundManager soundManager;
  private boolean isChallengeInProgress = false;
  private LevelManager levelManager;
  private Configurator configurator;
  private String experimentName;

  public AbstractExperimentView(String experimentName, 
      IExperimentModel experimentModel, float width, float height, Skin skin, 
      SoundManager soundManager) {
    super(width, height, true);
    this.experimentName = experimentName;
    this.skin = skin;
    this.soundManager = soundManager;
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

  public void setConfigurator(Configurator configurator) {
    this.configurator = configurator;
    this.addActor(configurator);
    this.levelManager = new LevelManager(this, configurator);
  }
}