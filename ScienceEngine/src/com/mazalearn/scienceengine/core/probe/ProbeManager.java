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
import com.mazalearn.scienceengine.core.view.IScience2DStage;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.experiments.ControlPanel;

/**
 * Cycles through the eligible registeredProbers - probing the user with each one.
 * 
 * @author sridhar
 * 
 */
public class ProbeManager extends Group implements IDoneCallback {
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

  public ProbeManager(final Skin skin, float width, float height,
      IScience2DStage science2DStage, ControlPanel controlPanel) {
    super();
    this.dashboard = new Dashboard(skin);
    this.addActor(dashboard);
    this.science2DStage = science2DStage;
    this.soundManager = ScienceEngine.getSoundManager();
    this.configGenerator = new ConfigGenerator(controlPanel.getModelConfigs());
    this.controlPanel = controlPanel;
    this.setPosition(0, 0);
    this.setSize(width, height);
     
    this.successImage = new ScoreImage(new Texture("images/greenballoon.png"), skin, true);
    this.failureImage = new ScoreImage(new Texture("images/redballoon.png"), skin, false);
    this.addActor(successImage);
    this.addActor(failureImage);
    probeHinter = new ProbeHinter(skin);
    probeHinter.setPosition(controlPanel.getX(),
        controlPanel.getY() + controlPanel.getPrefHeight() / 2 + 42);
    this.addActor(probeHinter);
  }

  public void registerProber(AbstractScience2DProber prober) {
    registeredProbers.add(prober);
    this.addActor(prober);
    prober.activate(false);
  }

  public void startChallenge() {
    // Reset scores
    dashboard.resetScore();
        
    // Make all actors non-movable
    for (Actor actor: this.getChildren()) {
      if (actor instanceof Science2DActor) {
        ((Science2DActor) actor).setAllowMove(false);
      }
    }

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
    }
    
    // Reinitialize active Probers
    for (AbstractScience2DProber prober: activeProbers) {
      prober.reinitialize(getX(), getY(), getWidth(), getHeight());
    }

    doProbe();
  }
  
  public void endChallenge() {
    // Turn on access to parts of control panel
    controlPanel.enableControls(true);
    science2DStage.done(false);
  }
  
  public List<Actor> getExcludedActors() {
    return this.excludedActors;
  }

  /**
   * IDoneCallback interface implementation
   */
  public void done(boolean success) {
    currentProber.activate(false);
    if (success) {
      soundManager.play(ScienceEngineSound.SUCCESS);
      dashboard.addScore(10);
      successImage.show(getWidth()/2, getHeight()/2, 10);
      probeHinter.setHint(null);
    } else {
      soundManager.play(ScienceEngineSound.FAILURE);
      dashboard.addScore(-5);
      failureImage.show(getWidth()/2, getHeight()/2, -5);
      String[] hints = currentProber.getHints();
      probeHinter.setHint(hints[MathUtils.random(hints.length - 1)]);
    }
    if (dashboard.getScore() > 10) {
      soundManager.play(ScienceEngineSound.CELEBRATE);
      science2DStage.done(true);
      this.setVisible(false);
      return;
    }
    doProbe();
  }

  // Prerequisite: activeProbers.size() >= 1
  private void doProbe() {
    // Move on to next active prober
    proberIndex = (proberIndex + 1) % activeProbers.size();
    currentProber = activeProbers.get(proberIndex);

    currentProber.addActor(probeHinter);
    currentProber.activate(true);
    dashboard.setStatus(currentProber.getTitle());
  }
  
  public void randomizeConfig() {
    configGenerator.generateConfig();
    // Turn off access to parts of control panel
    controlPanel.enableControls(false);
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