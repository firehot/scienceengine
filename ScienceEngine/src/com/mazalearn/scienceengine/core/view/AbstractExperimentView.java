package com.mazalearn.scienceengine.core.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.LevelManager;
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.core.model.IExperimentModel;
import com.mazalearn.scienceengine.experiments.ControlPanel;

public abstract class AbstractExperimentView extends Stage implements IExperimentView {

  protected final IExperimentModel experimentModel;
  protected final Skin skin;
  private boolean isChallengeInProgress = false;
  private LevelManager levelManager;
  protected ControlPanel controlPanel;
  private List<List<Actor>> locationGroups;
  private Vector2 deltaPosition = new Vector2();

  public AbstractExperimentView( 
      IExperimentModel experimentModel, float width, float height, Skin skin) {
    super(width, height, true);
    this.skin = skin;
    this.experimentModel = experimentModel;
    this.locationGroups = new ArrayList<List<Actor>>();
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
    // Reinitialize level
    experimentModel.reset();
    // Turn off/on access to parts of control panel
    controlPanel.enableControls(!challenge);
    if (challenge) {
      // Turn off music
      ScienceEngine.getMusicManager().setEnabled(false);
      // Make all actors non-movable
      for (Actor actor: getActors()) {
        if (actor instanceof ScienceActor) {
          ((ScienceActor) actor).setAllowDrag(false);
        }
      }
    } else {
      // Turn on music
      ScienceEngine.getMusicManager().play(ScienceEngineMusic.LEVEL);
    }
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

  public void setControlPanel(ControlPanel controlPanel) {
    this.controlPanel = controlPanel;
    this.addActor(controlPanel);
    this.levelManager = new LevelManager(this, controlPanel);
  }
}