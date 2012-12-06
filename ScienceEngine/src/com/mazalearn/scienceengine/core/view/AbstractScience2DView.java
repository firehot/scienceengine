package com.mazalearn.scienceengine.core.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.app.utils.PlatformAdapter.Platform;
import com.mazalearn.scienceengine.core.guru.AbstractScience2DProber;
import com.mazalearn.scienceengine.core.guru.LearningProber;
import com.mazalearn.scienceengine.core.guru.ParameterDirectionProber;
import com.mazalearn.scienceengine.core.guru.ParameterMagnitudeProber;
import com.mazalearn.scienceengine.core.guru.ProbeManager;
import com.mazalearn.scienceengine.core.model.ComponentType;
import com.mazalearn.scienceengine.core.model.IComponentType;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;

public abstract class AbstractScience2DView extends Stage implements IScience2DView {

  protected final IScience2DModel science2DModel;
  protected final Skin skin;
  private boolean isChallengeInProgress = false;
  protected ControlPanel controlPanel;
  private List<List<Actor>> locationGroups;
  private Vector2 deltaPosition = new Vector2();
  private ProbeManager probeManager;

  public AbstractScience2DView( 
      IScience2DModel science2DModel, float width, float height, Skin skin) {
    super(width, height, true);
    this.skin = skin;
    this.science2DModel = science2DModel;
    this.locationGroups = new ArrayList<List<Actor>>();
  }

  @Override
  public ProbeManager getProbeManager() {
    if (probeManager == null) {
      probeManager = new ProbeManager(skin, getWidth(), getHeight(), this, controlPanel);
      this.getRoot().addActor(controlPanel); // Move control Panel to top - why?
      // Add probeManager before controlpanel so that controls are accessible.
      this.getRoot().addActorBefore(controlPanel, probeManager);
    }
    return probeManager;
  }
  
  @Override
  public Actor addVisualActor(String name) {
    Actor actor = createActor(name);
    if (actor == null) return null;
    actor.setName(name);
    this.addActor(actor);
    return actor;
  }

  @Override
  public Actor addScience2DActor(Science2DBody body) {
    Actor actor = createActor(body);
    if (actor == null) return null;
    
    this.addActor(actor);
    return actor;
  }
  
  // Factory method for creating science2D actors
  protected Actor createActor(Science2DBody body) {
    IComponentType componentType = body.getComponentType();
    if (componentType == ComponentType.Dummy) {
      Pixmap pixmap = new Pixmap(8, 8, Format.RGBA8888);
      pixmap.setColor(Color.LIGHT_GRAY);
      pixmap.fillRectangle(0, 0, 8, 8);
      TextureRegion textureRegion = new TextureRegion(new Texture(pixmap));
      pixmap.dispose();
      return new Science2DActor(body, textureRegion);
    } else if (componentType == ComponentType.Environment) {
      TextureRegion textureRegion = 
          new TextureRegion(new Texture("images/environment.jpg"));
      Science2DActor science2DActor = new Science2DActor(body, textureRegion);
      science2DActor.setPositionFromViewCoords(false);
      return science2DActor;      
    }
    return null;
  }
  
  // Factory method for creating visual actors
  protected abstract Actor createActor(String type);
  
  @Override
  public void suspend(boolean suspend) {
    science2DModel.enable(!suspend);
  }

  @Override
  public boolean isSuspended() {
    return !science2DModel.isEnabled();
  }
  
  public void done(boolean success) {}
  
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
        groupActor.setX(groupActor.getX() + deltaX);
        groupActor.setY(groupActor.getY() + deltaY);
        if (groupActor instanceof Science2DActor) {
          ((Science2DActor) groupActor).setPositionFromViewCoords(false);
        }
      }
    }
  }
  
  @Override
  public void prepareStage() {
    // Register help after all actors are already added so it is on top
    //Actor help = new Helper(skin, 650, getHeight()  - 90);
    //this.addActor(help);
  }

  public void setControlPanel(ControlPanel controlPanel) {
    this.controlPanel = controlPanel;
    // Register control panel
    this.addActor(controlPanel);
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
  
  @Override
  public AbstractScience2DProber createProber(String proberName, ProbeManager probeManager) {
    if ("ParameterMagnitudeProber".equals(proberName)) {
      return new ParameterMagnitudeProber(science2DModel, probeManager);
    } else if ("ParameterDirectionProber".equals(proberName)) {
      return new ParameterDirectionProber(science2DModel, probeManager);
    } else if ("LearningProber".equals(proberName)) {
      return new LearningProber(science2DModel, probeManager);
    }
    return null;
  }

}