package com.mazalearn.scienceengine.core.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.app.screens.DomainHomeScreen;
import com.mazalearn.scienceengine.app.screens.LoadingScreen;
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter.Platform;
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

  public AbstractScience2DView( 
      IScience2DModel science2DModel, float width, float height, Skin skin, 
      IScience2DController controller) {
    super(width, height, true);
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
  
  public void done(boolean success) {
    if (success) {
      // TODO: put in a proper celebration here
      science2DController.getGuru().setTitle("Congratulations! You move to the next Level ");
      // TODO: generalize
      ScienceEngine.getPlatformAdapter().showInternalURL(
          "data/" + science2DController.getName() + "/" + science2DController.getLevel() + ".html");
      challenge(false);
    } else {
      // TODO: lack of symmetry here - cleanup required
      isChallengeInProgress = false;      
    }
  }
  
  public BitmapFont getFont() {
    return skin.getFont("default-font");
  }

  @Override
  public Actor findActor(String name) {
    return getRoot().findActor(name);
  }

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
    if (challenge) {
      science2DController.getGuru().startChallenge();
    } else {
      science2DController.getGuru().endChallenge();
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
    // Register help after all actors are already added so it is on top
    //Actor help = new Helper(skin, 650, getHeight()  - 90);
    //this.addActor(help);
    for (Actor actor: this.getActors()) {
      if (actor instanceof Science2DActor) {
        ((Science2DActor) actor).prepareActor();
      }
    }
  }

  public void setControlPanel(ControlPanel controlPanel) {
    this.controlPanel = controlPanel;
    // Register control panel
    this.addActor(controlPanel);
    // register the back button
    this.addActor(createBackButton());
    
    // Register stage components
    for (StageComponent stageComponent: StageComponent.values()) {
      Label component = new Label("", skin);
      float x = stageComponent.getX();
      if (x < 0) {
        x = getWidth() - x;
      }
      float y = stageComponent.getY();
      if (y < 0) {
        y = getHeight() - y;
      }
      component.setPosition(x, y);
      component.setName(stageComponent.name());
      component.setColor(stageComponent.getColor());
      this.addActor(component);
    }
    // If GWT, make status a disclaimer about experiencing on Android Tablet
    if (ScienceEngine.getPlatformAdapter().getPlatform() == Platform.GWT) {
      Label status = (Label) findActor(StageComponent.Status.name());
      status.setText("Demo only. Best experienced on Android Tablet");
    }
  }

  private Actor createBackButton() {
    final TextButton backButton = 
        new TextButton(ScienceEngine.getMsg().getString("ControlPanel.Back"), skin); //$NON-NLS-1$
    backButton.setName("BackButton");
    backButton.setPosition(5, getHeight() - 30);
    backButton.setWidth(80);
    backButton.addListener(new ClickListener() {
      public void clicked(InputEvent event, float x, float y) {
        AbstractScience2DView.this.challenge(false);
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        ScienceEngine.getProfileManager().retrieveProfile().setCurrentLevel(0);
        AbstractScreen screen = new DomainHomeScreen(ScienceEngine.SCIENCE_ENGINE, science2DController.getName());
        ScienceEngine.SCIENCE_ENGINE.setScreen(
            new LoadingScreen(ScienceEngine.SCIENCE_ENGINE, screen));
      }
      
      @Override
      public boolean touchDown(InputEvent event, float localX, float localY, int pointer, int button) {
        super.touchDown(event, localX, localY, pointer, button);
        IScience2DView stage = (IScience2DView) backButton.getStage();
        Label status = (Label) stage.findActor(StageComponent.Status.name());
        status.setText(ScienceEngine.getMsg().getString("Help.Back"));
        return true;
      }
    });
    return backButton;
  }

  @Override
  public void checkGuruProgress() {
    science2DController.getGuru().checkProgress();
  }
  
}