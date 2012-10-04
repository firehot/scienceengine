package com.mazalearn.scienceengine.core.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.LevelManager;
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.experiments.ControlPanel;

public abstract class AbstractScience2DStage extends Stage implements IScience2DStage {

  protected final IScience2DModel science2DModel;
  protected final Skin skin;
  private boolean isChallengeInProgress = false;
  private LevelManager levelManager;
  protected ControlPanel controlPanel;
  private List<List<Actor>> locationGroups;
  private Vector2 deltaPosition = new Vector2();

  public AbstractScience2DStage( 
      IScience2DModel science2DModel, float width, float height, Skin skin) {
    super(width, height, true);
    this.skin = skin;
    this.science2DModel = science2DModel;
    this.locationGroups = new ArrayList<List<Actor>>();
  }

  @Override
  public void suspend(boolean suspend) {
    science2DModel.enable(!suspend);
  }

  @Override
  public boolean isSuspended() {
    return !science2DModel.isEnabled();
  }
  
  public void done(boolean success) {}
  
  @Override
  public void challenge(boolean challenge) {
    // Reinitialize level
    science2DModel.reset();

    // Turn on or turn off music
    if (challenge) {
      ScienceEngine.getMusicManager().setEnabled(false);
    } else {
      ScienceEngine.getMusicManager().play(ScienceEngineMusic.LEVEL);
    }
    isChallengeInProgress = challenge;
  }
    
  @Override
  public boolean isChallengeInProgress() {
    return isChallengeInProgress;
  }
  
  @Override
  public void act(float delta) {
    science2DModel.simulateSteps(delta);
    super.act(delta);
  }
  
  public LevelManager getLevelManager() {
    return levelManager;
  }
  
  public void addLocationGroup(Actor... actors) {
    locationGroups.add(Arrays.asList(actors));
  }
  
  public void notifyLocationChangedByUser(Science2DActor actor, Vector2 newBodyPosition, float newBodyAngle) {
    for (List<Actor> locationGroup: locationGroups) {
      if (!locationGroup.contains(actor)) continue;
      deltaPosition.set(newBodyPosition)
          .sub(actor.getBody().getPosition())
          .mul(ScienceEngine.PIXELS_PER_M);
      float deltaX = deltaPosition.x;
      float deltaY = deltaPosition.y;
      float deltaAngle = (newBodyAngle - actor.getBody().getAngle()) % (2 * MathUtils.PI);
      float originX = actor.getBody().getPosition().x * ScienceEngine.PIXELS_PER_M;
      float originY = actor.getBody().getPosition().y * ScienceEngine.PIXELS_PER_M;
      for (Actor groupActor: locationGroup) {
        if (groupActor == actor || groupActor == null) continue;
        groupActor.parentToLocalCoordinates(deltaPosition);
        // For rotation - not yet working and wrong
        //groupActor.setOriginX(originX);
        //groupActor.setOriginY(originY);
        //groupActor.setRotation(groupActor.getRotation() + (deltaAngle * MathUtils.radiansToDegrees));
        // For translation
        groupActor.setX(groupActor.getX() + deltaX);
        groupActor.setY(groupActor.getY() + deltaY);
        if (groupActor instanceof Science2DActor) {
          ((Science2DActor) groupActor).setPositionFromViewCoords(false);
        }
      }
    }
  }

  public void setControlPanel(ControlPanel controlPanel) {
    this.controlPanel = controlPanel;
    this.addActor(controlPanel);
    this.levelManager = new LevelManager(this, science2DModel, controlPanel);
  }
}