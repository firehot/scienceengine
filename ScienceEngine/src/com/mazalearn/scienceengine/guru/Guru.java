package com.mazalearn.scienceengine.guru;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.services.SoundManager;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.ComponentType;
import com.mazalearn.scienceengine.core.model.Parameter;
import com.mazalearn.scienceengine.core.view.ModelControls;
import com.mazalearn.scienceengine.core.view.ViewControls;

/**
 * Cycles through the eligible registeredTutors - probing the user with each one.
 * 
 * @author sridhar
 * 
 */
public class Guru extends Group implements IDoneCallback {  
  int tutorIndex = -1;
  ITutor currentTutor;
  protected Dashboard dashboard;
  private List<ITutor> registeredTutors = new ArrayList<ITutor>();
  private List<Actor> excludedActors = new ArrayList<Actor>();
  private final ModelControls modelControls;
  private final ConfigGenerator configGenerator;
  private final SoundManager soundManager;
  private final ScoreImage successImage, failureImage;
  private Hinter hinter;
  private int deltaSuccessScore;
  private int deltaFailureScore;
  private IScience2DController science2DController;
  private ViewControls viewControls;
  
  public Guru(final Skin skin, IScience2DController science2DController) {
    super();
    this.science2DController = science2DController;

    this.setPosition(0, 0);
    // Guru has no direct user interaction - hence 0 size
    this.setSize(0, 0);
    
    this.dashboard = new Dashboard(skin);
    this.addActor(dashboard);
    dashboard.setY(ScreenComponent.VIEWPORT_HEIGHT - dashboard.getPrefHeight() / 2);
    dashboard.setX(ScreenComponent.VIEWPORT_WIDTH/2);
    
    this.soundManager = ScienceEngine.getSoundManager();
    this.configGenerator = new ConfigGenerator();
    this.modelControls = science2DController.getModelControls();
    this.viewControls = science2DController.getViewControls();
     
    this.successImage = new ScoreImage(new Texture("images/greenballoon.png"), skin, true);
    this.failureImage = new ScoreImage(new Texture("images/redballoon.png"), skin, false);
    ((Stage)science2DController.getView()).addActor(successImage);
    ((Stage)science2DController.getView()).addActor(failureImage);
    hinter = new Hinter(dashboard, skin);
    this.setVisible(false);
  }

  public void registerTutor(AbstractTutor tutor) {
    registeredTutors.add(tutor);
    this.addActor(tutor);
    // Move hinter to top
    this.addActor(hinter);
    tutor.activate(false);
  }

  public void startChallenge() {
    // Mark start of challenge in event log
    ScienceEngine.getEventLog().logEvent(ComponentType.Global.name(), 
        Parameter.Challenge.name());
    // Reset scores
    dashboard.resetScore();
    
    // Collect actors to be excluded from probe points.
    // These are the visible actors.
    excludedActors.clear();
    excludedActors.add(dashboard);
    for (Actor actor: science2DController.getView().getActors()) {
      if (actor.isVisible() && actor != this && !ScreenComponent.Background.name().equals(actor.getName())) {
        excludedActors.add(actor);
      }
    }
    
    if (registeredTutors.size() == 0) { // No tutors available
      endChallenge();
      return;
    }
    
    this.setVisible(true);
    tutorIndex = -1;
    runTutor();
  }
  
  public void endChallenge() {
    // Reinitialize current prober, if any
    if (currentTutor != null) {
      currentTutor.reinitialize(false);
      currentTutor = null;
    }

    science2DController.getView().done(false);
    ScienceEngine.setProbeMode(false);
    this.setVisible(false);
    // Clear event log
    ScienceEngine.getEventLog().clear();
  }
  
  public List<Actor> getExcludedActors() {
    return this.excludedActors;
  }

  /**
   * IDoneCallback interface implementation - probe is completed
   */
  public void done(boolean success) {
    if (success) {
      soundManager.play(ScienceEngineSound.SUCCESS);
      dashboard.addScore(deltaSuccessScore);
      successImage.show(deltaSuccessScore);
      hinter.clearHint();
      
      if (currentTutor.hasSucceeded()) {
        currentTutor.doSuccessActions();
        hinter.setHint(null);
        currentTutor.activate(false);
        currentTutor.reinitialize(false);
        
        // Success and no more tutors == WIN
        if (tutorIndex >= registeredTutors.size() - 1) {
          soundManager.play(ScienceEngineSound.CELEBRATE);
          science2DController.getView().done(true);
          this.setVisible(false);
          return;
        }
      }
      runTutor();
    } else {
      soundManager.play(ScienceEngineSound.FAILURE);
      // Equate success and failure scores so that 0 progress after second try
      deltaSuccessScore = deltaFailureScore;
      dashboard.addScore(-deltaFailureScore);
      failureImage.show(-deltaFailureScore);
      // Loss
      if (currentTutor.hasFailed()) {
        science2DController.getView().done(false);
        this.setVisible(false);
        return;
      }
    }
  }
  
  @Override
  public void act(float dt) {
    super.act(dt);
    if (Math.round(ScienceEngine.getTime()) % 2 != 0) return;
    if (currentTutor != null) {
      // Place hinter to right of dashboard above the controls
      hinter.setPosition(modelControls.getX(), ScreenComponent.VIEWPORT_HEIGHT - getY() - 50);
      if (!hinter.hasHint()) {
        hinter.setHint(currentTutor.getHint());
      }
    }
  }
  
  // Prerequisite: registeredTutors.size() >= 1
  private void runTutor() {
    // If a valid tutor is already running, let it continue
    if (currentTutor != null && !currentTutor.hasSucceeded()) {
      currentTutor.activate(true);
      return;
    }
    
    // Move on to next tutor
    tutorIndex++;
    if (tutorIndex == registeredTutors.size()) {
      done(true);
      return;
    }
    currentTutor = registeredTutors.get(tutorIndex);
    currentTutor.reinitialize(true);
    currentTutor.activate(true);
    dashboard.setGoal(currentTutor.getGoal());
    // Set up initial success and failure scores
    deltaSuccessScore = currentTutor.getSuccessScore();
    deltaFailureScore = currentTutor.getFailureScore();
  }
  
  public void setupProbeConfigs(List<IModelConfig<?>> configs, boolean enableControls) {
    configGenerator.generateConfig(configs);
    modelControls.syncWithModel(); // Force sync with model
    modelControls.refresh();
    // Turn off access to parts of control panel
    modelControls.enableControls(enableControls);
    viewControls.enableControls(enableControls);
  }

  public void setGoal(String text) {
    dashboard.setGoal(text);
  }

  public void checkProgress() {
    if (currentTutor == null) return;
    currentTutor.checkProgress();
  }

  public void reset() {
    if (currentTutor != null) {
      currentTutor.reset();
    }
  }
}