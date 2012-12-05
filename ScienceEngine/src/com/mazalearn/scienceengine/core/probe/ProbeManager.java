package com.mazalearn.scienceengine.core.probe;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.SoundManager;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.model.Science2DBody.MovementMode;
import com.mazalearn.scienceengine.core.view.ControlPanel;
import com.mazalearn.scienceengine.core.view.IScience2DStage;
import com.mazalearn.scienceengine.core.view.Science2DActor;

/**
 * Cycles through the eligible registeredProbers - probing the user with each one.
 * 
 * @author sridhar
 * 
 */
public class ProbeManager extends Group implements IDoneCallback {
  private static final int WIN_THRESHOLD = 100;
  private static final int LOSS_THRESHOLD = -30;
  
  int proberIndex = 0;
  AbstractScience2DProber currentProber;
  protected Dashboard dashboard;
  private List<AbstractScience2DProber> registeredProbers = new ArrayList<AbstractScience2DProber>();
  private List<AbstractScience2DProber> activeProbers = new ArrayList<AbstractScience2DProber>();
  private List<Actor> excludedActors = new ArrayList<Actor>();
  private final IScience2DStage science2DStage;
  private final ControlPanel controlPanel;
  private final ConfigGenerator configGenerator;
  private final SoundManager soundManager;
  private final ScoreImage successImage, failureImage;
  private ProbeHinter probeHinter;
  private int deltaSuccessScore;
  private int deltaFailureScore;

  public ProbeManager(final Skin skin, float width, float height,
      IScience2DStage science2DStage, ControlPanel controlPanel) {
    super();
    this.dashboard = new Dashboard(skin);
    this.addActor(dashboard);
    this.science2DStage = science2DStage;
    this.soundManager = ScienceEngine.getSoundManager();
    this.configGenerator = new ConfigGenerator();
    this.controlPanel = controlPanel;
    this.setPosition(0, 0);
    this.setSize(width, height);
     
    this.successImage = new ScoreImage(new Texture("images/greenballoon.png"), skin, true);
    this.failureImage = new ScoreImage(new Texture("images/redballoon.png"), skin, false);
    this.addActor(successImage);
    this.addActor(failureImage);
    probeHinter = new ProbeHinter(skin);
    // Place hinter to right of question mark above the controls.
    probeHinter.setPosition(controlPanel.getX(),
        controlPanel.getY() + controlPanel.getPrefHeight() / 2 + 20);
    this.addActor(probeHinter);
    this.setVisible(false);
  }

  public void registerProber(AbstractScience2DProber prober) {
    registeredProbers.add(prober);
    this.addActor(prober);
    prober.activate(false);
  }

  public void startChallenge() {
    // Reset scores
    dashboard.resetScore();
    
    // Collect actors to be excluded from probe points.
    // These are the visible actors.
    excludedActors.clear();
    excludedActors.add(dashboard);
    for (Actor actor: science2DStage.getActors()) {
      if (actor.isVisible() && actor != this) {
        excludedActors.add(actor);
      }
    }
    // Find active registeredProbers
    activeProbers.clear();
    for (AbstractScience2DProber prober: registeredProbers) {
      if (prober.isAvailable()) {
        activeProbers.add(prober);
      } else {
        prober.activate(false);
      }
    }
    
    if (activeProbers.size() == 0) { // No active probers available
      endChallenge();
      return;
    }
    
    // Reinitialize active Probers
    for (AbstractScience2DProber prober: activeProbers) {
      prober.reinitialize(getX(), getY(), getWidth(), getHeight(), true);
    }

    this.setVisible(true);
    doProbe();
  }
  
  public void endChallenge() {
    // Reinitialize current prober, if any
    if (currentProber != null) {
      currentProber.reinitialize(getX(), getY(), getWidth(), getHeight(), false);
    }

    // Turn on access to parts of control panel
    controlPanel.enableControls(true);
    science2DStage.done(false);
    ScienceEngine.setProbeMode(false);
    this.setVisible(false);
  }
  
  public List<Actor> getExcludedActors() {
    return this.excludedActors;
  }

  /**
   * IDoneCallback interface implementation
   */
  public void done(boolean success) {
    if (success) {
      currentProber.activate(false);
      currentProber.reinitialize(getX(), getY(), getWidth(), getHeight(), false);
      soundManager.play(ScienceEngineSound.SUCCESS);
      dashboard.addScore(deltaSuccessScore);
      successImage.show(getWidth()/2, getHeight()/2, deltaSuccessScore);
      probeHinter.setHint(null);
      doProbe();
    } else {
      soundManager.play(ScienceEngineSound.FAILURE);
      // Equate success and failure scores so that 0 progress after second try
      deltaSuccessScore = currentProber.getSubsequentDeltaSuccessScore();
      dashboard.addScore(deltaFailureScore);
      failureImage.show(getWidth()/2, getHeight()/2, deltaFailureScore);
      String[] hints = currentProber.getHints();
      if (hints.length > 0) {
        probeHinter.setHint(hints[MathUtils.random(hints.length - 1)]);
      }
    }
    // Win
    if (dashboard.getScore() >= WIN_THRESHOLD) {
      soundManager.play(ScienceEngineSound.CELEBRATE);
      science2DStage.done(true);
      this.setVisible(false);
      return;
    }
    // Loss
    if (dashboard.getScore() <= LOSS_THRESHOLD) {
      science2DStage.done(false);
      this.setVisible(false);
      return;
    }
  }

  // Prerequisite: activeProbers.size() >= 1
  private void doProbe() {
    // Move on to next active prober
    proberIndex = (proberIndex + 1) % activeProbers.size();
    currentProber = activeProbers.get(proberIndex);

    // Set up initial success and failure scores
    deltaSuccessScore = currentProber.getDeltaSuccessScore();
    deltaFailureScore = currentProber.getDeltaFailureScore();
    
    currentProber.addActor(probeHinter);
    currentProber.activate(true);
    dashboard.setStatus(currentProber.getTitle());
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

  public Actor findStageActor(String name) {
    for (Actor actor: science2DStage.getActors()) {
      if (name.equals(actor.getName())) return actor;
    }
    return null;
  }
}