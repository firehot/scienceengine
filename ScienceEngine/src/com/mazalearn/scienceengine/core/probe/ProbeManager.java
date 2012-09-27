package com.mazalearn.scienceengine.core.probe;

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
import com.mazalearn.scienceengine.experiments.electromagnetism.probe.AbstractFieldProber;

/**
 * Cycles through the probers - probing the user with each one.
 * 
 * @author sridhar
 * 
 */
public class ProbeManager extends Group implements IDoneCallback {
  int currentProber = 0;
  protected Dashboard dashboard;
  private List<AbstractFieldProber> probers = new ArrayList<AbstractFieldProber>();
  private final IDoneCallback doneCallback;
  private final ConfigGenerator configGenerator;
  private final SoundManager soundManager;
  private final ScoreImage successImage, failureImage;

  public ProbeManager(final Skin skin, float width, float height,
      List<IModelConfig<?>> modelConfigs, IDoneCallback doneCallback) {
    super();
    this.dashboard = new Dashboard(skin);
    this.addActor(dashboard);
    this.doneCallback = doneCallback;
    this.soundManager = ScienceEngine.getSoundManager();
    this.configGenerator = new ConfigGenerator(modelConfigs);
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

  public void addProbe(AbstractFieldProber prober) {
    probers.add(prober);
    this.addActor(prober);
    prober.activate(false);
  }

  public void startChallenge() {
    // Set up space for probers
    dashboard.resetScore();
    for (Actor prober: probers) {
      prober.x = x;
      prober.y = y;
      prober.width = width;
      prober.height = height;
    }
    doProbe();
  }

  /**
   * IDoneCallback interface implementation
   */
  public void done(boolean success) {
    soundManager.play(
        success ? ScienceEngineSound.SUCCESS : ScienceEngineSound.FAILURE);
    dashboard.addScore(success ? 10 : -5);
    if (success) {
      successImage.show(width/2, height/2, 10);
    } else {
      failureImage.show(width/2, height/2, -5);
    }
    probers.get(currentProber).activate(false);
    if (dashboard.getScore() > 100) {
      doneCallback.done(true);
      return;
    }
    // Move on to next prober, if available
    currentProber = (currentProber + 1) % probers.size();
    doProbe();
  }

  private void doProbe() {
    configGenerator.generateConfig();
    probers.get(currentProber).activate(true);
    dashboard.setStatus(probers.get(currentProber).getTitle());
  }

  public void setTitle(String text) {
    dashboard.setStatus(text);
  }

  public Actor getDashboard() {
    return dashboard;
  }
}