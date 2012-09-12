package com.mazalearn.scienceengine.view;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.experiments.electromagnetism.AbstractProber;

/**
 * Cycles through the probers - probing the user with each one.
 * @author sridhar
 *
 */
public class ProbeManager extends Group {
  int current = 0;
  protected Dashboard dashboard;
  private List<AbstractProber> probers = new ArrayList<AbstractProber>();
  
  public ProbeManager(Skin skin, Stage view) {
    super();
    this.dashboard = new Dashboard(skin);
    this.addActor(dashboard);
    this.x = 0;
    this.y = 0;
    this.width = view.width();
    this.height = view.height();
    view.addActor(this);
  }
  
  public void add(AbstractProber prober) {
    probers.add(prober);
    this.addActor(prober);
    prober.activate(false);
  }
  
  public void startChallenge() {
    probers.get(current).activate(true);
    dashboard.setTitle(probers.get(current).getTitle());
  }
  
  public void probeDone(boolean success) {
    dashboard.addScore(success ? 10 : -5);
    probers.get(current).activate(false);
    current = (current + 1) % probers.size();
    probers.get(current).activate(true);
    dashboard.setTitle(probers.get(current).getTitle());
  }
}