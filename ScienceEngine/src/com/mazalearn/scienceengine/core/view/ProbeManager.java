package com.mazalearn.scienceengine.core.view;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.SoundManager;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.experiments.electromagnetism.AbstractProber;

/**
 * Cycles through the probers - probing the user with each one.
 * 
 * @author sridhar
 * 
 */
public class ProbeManager extends Group implements IDoneCallback {
  int current = 0;
  protected Dashboard dashboard;
  private List<AbstractProber> probers = new ArrayList<AbstractProber>();
  private final IDoneCallback doneCallback;
  private final SoundManager soundManager;

  public ProbeManager(Skin skin, float width, float height,
      IDoneCallback doneCallback) {
    super();
    this.dashboard = new Dashboard(skin);
    this.addActor(dashboard);
    this.doneCallback = doneCallback;
    this.soundManager = ScienceEngine.getSoundManager();
    this.x = 0;
    this.y = 0;
    this.width = width;
    this.height = height;
    this.dashboard.y = height - dashboard.getPrefHeight();
    this.dashboard.x = width/2 - dashboard.getPrefWidth()/2;
    //this.dashboard.originX = 
  }

  public void addProbe(AbstractProber prober) {
    probers.add(prober);
    this.addActor(prober);
    prober.activate(false);
  }

  public void startChallenge() {
    // Set up space for probers
    for (Actor prober: probers) {
      prober.x = x;
      prober.y = y;
      prober.width = width;
      prober.height = height;
    }
    probers.get(current).activate(true);
    dashboard.setStatus(probers.get(current).getTitle());
  }

  /**
   * IDoneCallback interface implementation
   */
  public void done(boolean success) {
    soundManager.play(
        success ? ScienceEngineSound.SUCCESS : ScienceEngineSound.FAILURE);
    dashboard.addScore(success ? 10 : -5);
    probers.get(current).activate(false);
    if (dashboard.getScore() > 100) {
      doneCallback.done(true);
      return;
    }
    current = (current + 1) % probers.size();
    probers.get(current).activate(true);
    dashboard.setStatus(probers.get(current).getTitle());
  }

  public void setTitle(String text) {
    dashboard.setStatus(text);
  }

  public Actor getDashboard() {
    return dashboard;
  }
}