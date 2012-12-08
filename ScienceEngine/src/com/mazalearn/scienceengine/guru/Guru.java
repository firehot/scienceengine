package com.mazalearn.scienceengine.guru;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.SoundManager;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.ControlPanel;
import com.mazalearn.scienceengine.core.view.IScience2DView;

/**
 * Cycles through the eligible registeredTutors - probing the user with each one.
 * 
 * @author sridhar
 * 
 */
public class Guru extends Group implements IDoneCallback {
  private static final int WIN_THRESHOLD = 100;
  private static final int LOSS_THRESHOLD = -30;
  
  int tutorIndex = -1;
  AbstractTutor currentTutor;
  protected Dashboard dashboard;
  private List<AbstractTutor> registeredTutors = new ArrayList<AbstractTutor>();
  private List<Actor> excludedActors = new ArrayList<Actor>();
  private final IScience2DView science2DView;
  private final ControlPanel controlPanel;
  private final ConfigGenerator configGenerator;
  private final SoundManager soundManager;
  private final ScoreImage successImage, failureImage;
  private Hinter hinter;
  private int deltaSuccessScore;
  private int deltaFailureScore;
  private float windowWidth;
  private float windowHeight;
  private IScience2DModel science2DModel;

  public Guru(final Skin skin, float width, float height,
      IScience2DView science2DView, IScience2DModel science2dModel, ControlPanel controlPanel) {
    super();
    this.dashboard = new Dashboard(skin);
    this.addActor(dashboard);
    this.science2DView = science2DView;
    this.science2DModel = science2DModel;
    this.soundManager = ScienceEngine.getSoundManager();
    this.configGenerator = new ConfigGenerator();
    this.controlPanel = controlPanel;
    this.windowWidth = width;
    this.windowHeight = height;
    this.setPosition(0, 0);
    this.setSize(width, height);
     
    this.successImage = new ScoreImage(new Texture("images/greenballoon.png"), skin, true);
    this.failureImage = new ScoreImage(new Texture("images/redballoon.png"), skin, false);
    this.addActor(successImage);
    this.addActor(failureImage);
    hinter = new Hinter(skin);
    // Place hinter to right of dashboard above the controls
    hinter.setPosition(controlPanel.getX(), 0);
    // Place hinter to right of question mark above the controls.
    //hinter.setPosition(controlPanel.getX(),
    //    controlPanel.getY() + controlPanel.getPrefHeight() / 2 + 20);
    this.addActor(hinter);
    this.setVisible(false);
  }

  public void registerTutor(AbstractTutor tutor) {
    registeredTutors.add(tutor);
    this.addActor(tutor);
    tutor.activate(false);
  }

  public void startChallenge() {
    // Reset scores
    dashboard.resetScore();
    
    // Collect actors to be excluded from probe points.
    // These are the visible actors.
    excludedActors.clear();
    excludedActors.add(dashboard);
    for (Actor actor: science2DView.getActors()) {
      if (actor.isVisible() && actor != this) {
        excludedActors.add(actor);
      }
    }
    
    if (registeredTutors.size() == 0) { // No guides available
      endChallenge();
      return;
    }
    
    // Reinitialize active guides
    for (AbstractTutor guide: registeredTutors) {
      guide.reinitialize(getX(), getY(), windowWidth, windowHeight, true);
    }

    this.setVisible(true);
    runTutor();
  }
  
  public void endChallenge() {
    // Reinitialize current prober, if any
    if (currentTutor != null) {
      currentTutor.reinitialize(getX(), getY(), windowWidth, windowHeight, false);
    }

    // Turn on access to parts of control panel
    controlPanel.enableControls(true);
    science2DView.done(false);
    ScienceEngine.setProbeMode(false);
    this.setVisible(false);
  }
  
  public List<Actor> getExcludedActors() {
    return this.excludedActors;
  }

  /**
   * IDoneCallback interface implementation - probe is completed
   */
  public void done(boolean success) {
    if (success) {
      this.setSize(windowWidth,  windowHeight);
      this.setPosition(0, 0);
      currentTutor.activate(false);
      currentTutor.reinitialize(getX(), getY(), getWidth(), getHeight(), false);
      soundManager.play(ScienceEngineSound.SUCCESS);
      dashboard.addScore(deltaSuccessScore);
      successImage.show(getWidth()/2, getHeight()/2, deltaSuccessScore);
      runTutor();
    } else {
      soundManager.play(ScienceEngineSound.FAILURE);
      // Equate success and failure scores so that 0 progress after second try
      deltaSuccessScore = currentTutor.getSubsequentDeltaSuccessScore();
      dashboard.addScore(deltaFailureScore);
      failureImage.show(getWidth()/2, getHeight()/2, deltaFailureScore);
    }
    // Win
    if (dashboard.getScore() >= WIN_THRESHOLD) {
      soundManager.play(ScienceEngineSound.CELEBRATE);
      science2DView.done(true);
      this.setVisible(false);
      return;
    }
    // Loss
    if (dashboard.getScore() <= LOSS_THRESHOLD) {
      science2DView.done(false);
      this.setVisible(false);
      return;
    }
  }
  
  @Override
  public void act(float dt) {
    super.act(dt);
    if (Math.round(ScienceEngine.getTime()) % 10 != 0) return;
    String hintText = currentTutor != null ? currentTutor.getHint() : null;
    hinter.setHint(hintText);
  }
  
  // Prerequisite: registeredTutors.size() >= 1
  private void runTutor() {
    // Move on to next tutor
    tutorIndex = (tutorIndex + 1) % registeredTutors.size();
    currentTutor = registeredTutors.get(tutorIndex);

    // Set up initial success and failure scores
    deltaSuccessScore = currentTutor.getDeltaSuccessScore();
    deltaFailureScore = currentTutor.getDeltaFailureScore();
    
    currentTutor.activate(true);
    dashboard.setStatus(currentTutor.getTitle());
  }
  
  public void setupProbeConfigs(List<IModelConfig<?>> configs, boolean enableControls) {
    configGenerator.generateConfig(configs);
    controlPanel.syncWithModel(); // Force sync with model
    controlPanel.refresh();
    // Turn off access to parts of control panel
    controlPanel.enableControls(enableControls);
  }

  public void setTitle(String text) {
    dashboard.setStatus(text);
  }

  public Actor findViewActor(String name) {
    for (Actor actor: science2DView.getActors()) {
      if (name.equals(actor.getName())) return actor;
    }
    return null;
  }
}