package com.mazalearn.scienceengine.core.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.screens.HelpTour;
import com.mazalearn.scienceengine.app.screens.HelpTour.IHelpComponent;
import com.mazalearn.scienceengine.app.screens.TutoringEndDialog;
import com.mazalearn.scienceengine.app.services.MusicManager.ScienceEngineMusic;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter.Platform;
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
  private Button goButton;
  private Group basicScreen;

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
      Dialog dialog = new TutoringEndDialog(this, skin, 
          ScienceEngine.getMsg().getString("Level.Success"));
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
    // Bring BasicScreen controls to top.
    this.addActor(basicScreen);
  }

  public void setupControls() {
    basicScreen = (Group) findActor(ScreenComponent.CORE_GROUP);
    if (basicScreen == null) {
      basicScreen = new Group();
      basicScreen.setName(ScreenComponent.CORE_GROUP);
      this.addActor(basicScreen);
    }
    // Create view and model controls
    this.viewControls = new ActivityViewControls(science2DController, skin);
    basicScreen.addActor(viewControls);

    this.modelControls = new ModelControls(science2DModel, skin);
    basicScreen.addActor(modelControls);
    
    this.goButton = createGoButton();
    basicScreen.addActor(goButton);
    // Help icon
    basicScreen.addActor(createHelpActor());

    // If GWT, display a disclaimer about experiencing on a Tablet
    if (ScienceEngine.getPlatformAdapter().getPlatform() == Platform.GWT) {
      ScienceEngine.displayStatusMessage(this, 
          "Partial Demo only. Best experienced on Android/iPad Tablet.");
    }
  }
  
  public ModelControls getModelControls() {
    return modelControls;
  }

  private Actor createHelpActor() {
    Image helpImage = new Image(ScienceEngine.getTextureRegion("help"));
    helpImage.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x1, float y1) {
        String description = ScienceEngine.getMsg().getString(science2DController.getTopic() + "." + 
            science2DController.getLevel() + ".Description");
        List<IHelpComponent> helpComponents = new ArrayList<IHelpComponent>();
        for (Actor actor: getActors()) {
          if ((actor instanceof Science2DActor)) {
            helpComponents.add((IHelpComponent) actor);
          }
        }
        for (ScreenComponent screenComponent: ScreenComponent.values()) {
          if (screenComponent.showInHelpTour()) {
            helpComponents.add(screenComponent);
          }
        }
        new HelpTour(AbstractScience2DView.this, skin, description, helpComponents);
      }
    });
    ScreenComponent sc = ScreenComponent.Help;
    helpImage.setPosition(sc.getX(), sc.getY());
    helpImage.setSize(sc.getWidth(), sc.getHeight());
    helpImage.setName(ScreenComponent.Help.name());
    return helpImage;
  }
  
  private Button createGoButton() {
    Drawable go = new TextureRegionDrawable(ScienceEngine.getTextureRegion("go"));
    final Button goButton = new Button(go);
    ScreenComponent goButtonUp = ScreenComponent.GoButtonUp;
    goButton.setSize(goButtonUp.getWidth(), goButtonUp.getHeight());
    goButton.setPosition(goButtonUp.getX(), goButtonUp.getY());
    goButton.addListener(new ClickListener() {
      @Override public void clicked(InputEvent event, float x, float y) {
        isTutoringInProgress = !isTutoringInProgress;
        ScreenComponent goButtonTo = 
            isTutoringInProgress ? ScreenComponent.GoButtonDown : ScreenComponent.GoButtonUp;
        goButton.addAction(Actions.parallel(
            Actions.moveTo(goButtonTo.getX(), goButtonTo.getY(), 1),
            Actions.sizeTo(goButtonTo.getWidth(), goButtonTo.getHeight(), 1)));
        tutoring(isTutoringInProgress);
      }
    });
    
    return goButton;
  }

  @Override
  public void checkGuruProgress() {
    science2DController.getGuru().checkProgress();
  }

  @Override
  public ViewControls getViewControls() {
    return viewControls;
  }
  
}