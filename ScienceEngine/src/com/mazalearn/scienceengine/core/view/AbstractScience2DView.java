package com.mazalearn.scienceengine.core.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.app.screens.InstructionDialog;
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter.Platform;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.IScience2DModel;

public class AbstractScience2DView extends Stage implements IScience2DView {

  protected final IScience2DModel science2DModel;
  protected final Skin skin;
  private boolean isChallengeInProgress = false;
  protected ControlPanel controlPanel;
  private List<List<Actor>> locationGroups;
  private Vector2 deltaPosition = new Vector2();
  private IScience2DController science2DController;
  // Commands at view level - possibly affecting multiple actors
  private List<IModelConfig<?>> viewCommands;
  private ViewControls viewControls;

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
    if (!isChallengeInProgress) return;
    
    if (success) {
      // TODO: put in a proper celebration here
      science2DController.getGuru().setGoal("Congratulations! You move to the next Level ");
      ScienceEngine.getPlatformAdapter().showInternalURL(
          "data/" + science2DController.getDomain() + "/" + science2DController.getLevel() + ".html");
      challenge(false);
      Dialog dialog = new InstructionDialog(this, skin, science2DController.getDomain(), 
          ScienceEngine.getMsg().getString("Level.Success"), 
          ScienceEngine.getMsg().getString("Level.Instructions"), "OK");
      dialog.show(this);      
    } else {
      // TODO: lack of symmetry here - cleanup required
      isChallengeInProgress = false;      
      Dialog dialog = new InstructionDialog(this, skin, science2DController.getDomain(), 
          ScienceEngine.getMsg().getString("Level.Failure"), 
          ScienceEngine.getMsg().getString("Level.Instructions"), "OK");
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
  public void challenge(boolean challenge) {
    // Turn on or turn off music
    if (challenge) {
      ScienceEngine.getMusicManager().setEnabled(false);
    } else {
      ScienceEngine.getMusicManager().play(ScienceEngineMusic.LEVEL);
    }
    isChallengeInProgress = challenge;

    if (challenge) {
      // Reinitialize level
      science2DController.reset();
      science2DController.getGuru().startChallenge();
    } else {
      science2DController.getGuru().endChallenge();
      // Reinitialize level
      science2DController.reset();
    }
  }
    
  @Override
  public boolean isChallengeInProgress() {
    return isChallengeInProgress;
  }
  
  @Override
  public void act(float delta) {
    ScienceEngine.addTimeElapsed(delta);
    if (isSuspended()) return;
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
          .mul(ScienceEngine.PIXELS_PER_M);
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
    this.addActor(viewControls);
    this.addActor(controlPanel);
  }

  public ControlPanel setupStage() {
    // Register stage components
    for (StageComponent stageComponent: StageComponent.values()) {
      Actor component = addStageComponent(stageComponent);
      float x = stageComponent.getX();
      if (x < 0) {
        x = AbstractScreen.VIEWPORT_WIDTH + x;
      }
      float y = stageComponent.getY();
      if (y < 0) {
        y = AbstractScreen.VIEWPORT_HEIGHT + y;
      }
      component.setPosition(x, y);
    }
    // If GWT, make status a disclaimer about experiencing on Android Tablet
    if (ScienceEngine.getPlatformAdapter().getPlatform() == Platform.GWT) {
      Label status = (Label) findActor(StageComponent.Status.name());
      status.setText("Demo only. Best experienced on Android/iPad Tablets.");
    }
    
    return controlPanel;
  }

  private Actor addStageComponent(StageComponent stageComponent) {
    Actor component = null;
    switch (stageComponent) {
      case Status: 
      case Title: {
        Table table = new Table(skin); 
        table.add(component = new Label("", skin));
        component.setName(stageComponent.name());
        component.setColor(stageComponent.getColor());
        this.addActor(table);
        return table;
      }
      case ViewControls: { 
        this.viewControls = new ViewControls(science2DController, skin);
        this.addActor(viewControls);
        viewControls.setName(stageComponent.name());
        return viewControls;
      }
      case ControlPanel: {
        this.controlPanel = new ControlPanel(science2DModel, skin);
        this.addActor(controlPanel);
        controlPanel.setName(stageComponent.name());
        return controlPanel;
      }
    }
    return component;
  }

  @Override
  public void checkGuruProgress() {
    science2DController.getGuru().checkProgress();
  }
  
}