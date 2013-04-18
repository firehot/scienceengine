package com.mazalearn.scienceengine.core.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.screens.TutoringEndDialog;
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.IScience2DModel;

public class AbstractScience2DView extends Stage implements IScience2DView {

  protected final IScience2DModel science2DModel;
  protected final Skin skin;
  private boolean isTutoringInProgress = false;
  protected ModelControls modelControls;
  private List<List<Actor>> locationGroups;
  private Vector2 deltaPosition = new Vector2();
  private IScience2DController science2DController;
  // Commands at view level - possibly affecting multiple actors
  private List<IModelConfig<?>> viewCommands;
  private ViewControls viewControls;
  private Group activityGroup;

  public AbstractScience2DView( 
      IScience2DModel science2DModel, float width, float height, Skin skin, 
      IScience2DController controller) {
    super(width, height, false);
    this.skin = skin;
    this.science2DModel = science2DModel;
    this.science2DController = controller;
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
  
  @Override
  public List<IModelConfig<?>> getCommands() {
    if (viewCommands == null) {
      viewCommands = new ArrayList<IModelConfig<?>>();
      initializeCommands(viewCommands);
    }
    return Collections.unmodifiableList(viewCommands);
  }
  
  public void initializeCommands(List<IModelConfig<?>> viewCommands) {    
  }
  
  public void done(boolean success) {
    if (!isTutoringInProgress) return;
    
    if (success) {
      ScienceEngine.getPlatformAdapter().showInternalURL(
          "data/" + science2DController.getTopic() + "/" + science2DController.getLevel() + ".html");
      tutoring(false);
      // TODO: Each level should have own success/failure message. 
      Dialog dialog = new TutoringEndDialog(this, skin, 
          ScienceEngine.getMsg().getString(science2DController.getTopic() + "." + 
              science2DController.getLevel() + ".End"));
      dialog.show(this);      
    } else {
      // TODO: lack of symmetry here - cleanup required
      isTutoringInProgress = false;      
      Dialog dialog = new TutoringEndDialog(this, skin, 
          ScienceEngine.getMsg().getString("Level.Failure"));
      dialog.show(this);      
    }
  }
  
  public BitmapFont getFont() {
    //return skin.getFont("default-font");
    return skin.getFont("en");
  }

  @Override
  public Actor findActor(String name) {
    return getRoot().findActor(name);
  }

  @Override
  public void tutoring(boolean tutoringOn) {
    // Turn on or turn off music
    if (tutoringOn) {
      ScienceEngine.getMusicManager().setEnabled(false);
    } else {
      ScienceEngine.getMusicManager().play(ScienceEngineMusic.LEVEL);
    }

    if (tutoringOn) {
      isTutoringInProgress = true;
      // Reinitialize level
      science2DController.reset();
      science2DController.getGuru().beginTutoring();
    } else if (isTutoringInProgress) {
      isTutoringInProgress = false;
      science2DController.getGuru().endTutoring();
      // Reinitialize level
      science2DController.reset();
    }
  }
  
  @Override
  public boolean isTutoringInProgress() {
    return isTutoringInProgress;
  }
  
  @Override
  public void act(float delta) {
    if (isSuspended()) return;
    ScienceEngine.addTimeElapsed(delta);
    science2DModel.simulateSteps(delta);
    super.act(delta);
  }
  
 public void addLocationGroup(Actor[] actors) {
    locationGroups.add(Arrays.asList(actors));
  }
  

  @Override
  public List<List<Actor>> getLocationGroups() {
    return locationGroups;
  }
  
  @Override
  public void removeLocationGroups() {
    locationGroups.clear();
  }
  
  public void notifyLocationChangedByUser(Science2DActor actor, Vector2 newBodyPosition, float newBodyAngle) {
    for (List<Actor> locationGroup: locationGroups) {
      if (!locationGroup.contains(actor)) continue;
      deltaPosition.set(newBodyPosition)
          .sub(actor.getBody().getPosition())
          .mul(ScreenComponent.PIXELS_PER_M);
      float deltaX = deltaPosition.x;
      float deltaY = deltaPosition.y;
      //float deltaAngle = (newBodyAngle - actor.getBody().getAngle()) % (2 * MathUtils.PI);
      //float originX = actor.getBody().getPosition().x * ScienceEngine.PIXELS_PER_M;
      //float originY = actor.getBody().getPosition().y * ScienceEngine.PIXELS_PER_M;
      for (Actor groupActor: locationGroup) {
        if (groupActor == actor || groupActor == null) continue;
        groupActor.parentToLocalCoordinates(deltaPosition);
        // For rotation - not yet working and wrong
        //groupActor.setOriginX(originX);
        //groupActor.setOriginY(originY);
        //groupActor.setRotation(groupActor.getRotation() + (deltaAngle * MathUtils.radiansToDegrees));
        // For translation
        groupActor.setPosition(groupActor.getX() + deltaX, groupActor.getY() + deltaY);
        if (groupActor instanceof Science2DActor) {
          ((Science2DActor) groupActor).setPositionFromViewCoords(false);
        }
      }
    }
  }
  
  @Override
  public void prepareView() {
    for (Actor actor: this.getActors()) {
      if (actor instanceof Science2DActor) {
        ((Science2DActor) actor).prepareActor();
      }
    }
    // Bring CoreGroup to top.
    Group coreGroup = (Group) findActor(ScreenComponent.CORE_GROUP);
    this.addActor(coreGroup);
    // Bring model controls above all other components in activity group
    activityGroup.addActor(modelControls);
  }
  
  public void setupControls() {
    activityGroup = new Group();
    activityGroup.setName(ScreenComponent.ACTIVITY_GROUP);
    this.addActor(activityGroup);

    // Create view and model controls
    this.viewControls = new ActivityViewControls(science2DController, skin);

    this.modelControls = new ModelControls(science2DModel, skin);
    activityGroup.addActor(modelControls);
  }
  
  public ModelControls getModelControls() {
    return modelControls;
  }

  @Override
  public void checkActiveTutorProgress() {
    science2DController.getGuru().checkProgress();
  }

  @Override
  public ViewControls getViewControls() {
    return viewControls;
  }
  
}