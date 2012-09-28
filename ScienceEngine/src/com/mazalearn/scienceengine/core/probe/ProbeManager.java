package com.mazalearn.scienceengine.core.probe;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.SoundManager;
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.view.IExperimentView;
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
  AbstractProber currentProber;
  protected Dashboard dashboard;
  private List<AbstractProber> registeredProbers = new ArrayList<AbstractProber>();
  private List<AbstractProber> activeProbers = new ArrayList<AbstractProber>();
  private List<Actor> excludedActors = new ArrayList<Actor>();
  private final IExperimentView experimentView;
  private final ControlPanel controlPanel;
  private final ConfigGenerator configGenerator;
  private final SoundManager soundManager;
  private final ScoreImage successImage, failureImage;

  public ProbeManager(final Skin skin, float width, float height,
      List<IModelConfig<?>> modelConfigs, IExperimentView experimentView, 
      ControlPanel controlPanel) {
    super();
    this.dashboard = new Dashboard(skin);
    this.addActor(dashboard);
    this.experimentView = experimentView;
    this.soundManager = ScienceEngine.getSoundManager();
    this.configGenerator = new ConfigGenerator(modelConfigs);
    this.controlPanel = controlPanel;
    this.x = 0;
    this.y = 0;
    this.width = width;
    this.height = height;
    // For a table, x and y are at center, top of table - not at bottom left
    this.dashboard.y = height;
    this.dashboard.x = width/2;
     
    this.successImage = new ScoreImage(new Texture("images/greenballoon.png"), skin, true);
    this.failureImage = new ScoreImage(new Texture("images/redballoon.png"), skin, false);
    this.addActor(successImage);
    this.addActor(failureImage);
  }

  public void registerProber(AbstractProber prober) {
    registeredProbers.add(prober);
    this.addActor(prober);
    prober.activate(false);
  }

  public void startChallenge() {
    // Reset scores
    dashboard.resetScore();
        
    // Make all actors non-movable
    for (Actor actor: getActors()) {
      if (actor instanceof Science2DActor) {
        ((Science2DActor) actor).setAllowMove(false);
      }
    }

    // Collect actors to be excluded from probe points.
    // These are the visible actors.
    excludedActors.clear();
    excludedActors.add(dashboard);
    for (Actor actor: experimentView.getActors()) {
      if (actor.visible && actor != this) {
        excludedActors.add(actor);
      }
    }
    // Find active registeredProbers
    activeProbers.clear();
    for (AbstractProber prober: registeredProbers) {
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
    for (AbstractProber prober: activeProbers) {
      prober.reinitialize(x, y, width, height);
    }

    doProbe();
  }
  
  private void endChallenge() {
    // Turn on access to parts of control panel
    controlPanel.enableControls(true);
    experimentView.done(false);
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
      successImage.show(width/2, height/2, 10);
    } else {
      soundManager.play(ScienceEngineSound.FAILURE);
      dashboard.addScore(-5);
      failureImage.show(width/2, height/2, -5);
    }
    if (dashboard.getScore() > 100) {
      experimentView.done(true);
      return;
    }
    doProbe();
  }

  // Prerequisite: activeProbers.size() >= 1
  private void doProbe() {
    // Move on to next active prober
    proberIndex = (proberIndex + 1) % activeProbers.size();
    currentProber = activeProbers.get(proberIndex);

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

  public Actor findActorByName(String name) {
    for (Actor actor: experimentView.getActors()) {
      if (actor.name.equals(name)) return actor;
    }
    return null;
  }
}