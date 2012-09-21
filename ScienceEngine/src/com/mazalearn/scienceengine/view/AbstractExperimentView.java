package com.mazalearn.scienceengine.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.box2d.ScienceActor;
import com.mazalearn.scienceengine.controller.Configurator;
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
  private List<List<Actor>> locationGroups;
  private Vector2 deltaPosition = new Vector2();
  private List<List<ScienceActor>> circuits;

  public AbstractExperimentView(String experimentName, 
      IExperimentModel experimentModel, float width, float height, Skin skin, 
      SoundManager soundManager) {
    super(width, height, true);
    this.experimentName = experimentName;
    this.skin = skin;
    this.soundManager = soundManager;
    this.experimentModel = experimentModel;
    this.locationGroups = new ArrayList<List<Actor>>();
    this.circuits = new ArrayList<List<ScienceActor>>();
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
  
  public void addLocationGroup(Actor... actors) {
    locationGroups.add(Arrays.asList(actors));
  }
  
  public void notifyLocationChangedByUser(ScienceActor actor, Vector2 newPosition) {
    for (List<Actor> locationGroup: locationGroups) {
      if (!locationGroup.contains(actor)) continue;
      deltaPosition.set(newPosition)
          .sub(actor.getBody().getPosition())
          .mul(ScienceEngine.PIXELS_PER_M);
      for (Actor groupActor: locationGroup) {
        if (groupActor == actor || groupActor == null) continue;
        groupActor.x += deltaPosition.x;
        groupActor.y += deltaPosition.y;
        if (groupActor instanceof ScienceActor) {
          ((ScienceActor) groupActor).setPositionFromViewCoords(false);
        }
      }
    }
  }

  public void addCircuit(ScienceActor... actors) {
    circuits.add(Arrays.asList(actors));
  }
  
  public void notifyCurrentChange(ScienceActor actor) {
    for (List<ScienceActor> circuit: circuits) {
      if (!circuit.contains(actor)) continue;
      for (Actor groupActor: circuit) {
        if (groupActor == actor || groupActor == null) continue;
        if (groupActor instanceof ScienceActor) {
        }
      }
    }
  }

  public void setConfigurator(Configurator configurator) {
    this.configurator = configurator;
    this.addActor(configurator);
    this.levelManager = new LevelManager(this, configurator);
  }
}