package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.services.loaders.LevelLoader;
import com.mazalearn.scienceengine.core.model.ComponentType;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.AbstractScience2DView;
import com.mazalearn.scienceengine.core.view.ModelControls;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.core.view.ViewControls;
import com.mazalearn.scienceengine.guru.AbstractTutor;
import com.mazalearn.scienceengine.guru.TutorGroup;
import com.mazalearn.scienceengine.guru.Guru;
import com.mazalearn.scienceengine.guru.ITutor;
import com.mazalearn.scienceengine.guru.ParameterProber;
import com.mazalearn.scienceengine.guru.Subgoal;

public abstract class AbstractScience2DController implements
    IScience2DController {

  protected ModelControls modelControls;
  protected ViewControls viewControls;
  protected IScience2DModel science2DModel;
  protected IScience2DView science2DView;
  protected Skin skin;
  private String domain;
  private int level;
  private Guru guru;

  protected AbstractScience2DController(String domain, int level, Skin skin) {
    this.domain = domain;
    this.level = level;
    this.skin = skin;
  }
  
  protected void initialize(IScience2DModel science2DModel, 
      AbstractScience2DView science2DView) {
    this.science2DModel = science2DModel;
    this.science2DView = science2DView;
    science2DView.setupControls();
    this.modelControls = science2DView.getModelControls();
    this.viewControls = science2DView.getViewControls();
  }
  
  @Override
  public String getDomain() {
    return domain;
  }
  
  @Override
  public String getTitle() {
    return ScienceEngine.getMsg().getString(domain + "." + level + ".Name");    
  }
  
  @Override
  public int getLevel() {
    return level;
  }
  
  @Override
  public IScience2DView getView() {
    return science2DView;
  }

  @Override
  public IScience2DModel getModel() {
    return science2DModel;
  }

  @Override
  public ModelControls getModelControls() {
    return modelControls;
  }
  
  @Override
  public ViewControls getViewControls() {
    return viewControls;
  }
  
  @Override
  public Skin getSkin() {
    return skin;
  }
  
  @Override
  public void reset() {
    new LevelLoader(this).reload();
    getGuru().reset();
  }

  @Override
  public Guru getGuru() {
    if (guru == null) {
      Stage stage = (Stage) science2DView;
      guru = new Guru(skin, this, this.getTitle());
      // Move control Panel to top - so it will be above others
      stage.getRoot().addActor(modelControls);
      // Move back button to top also - so it will be accessible
      Actor backButton = stage.getRoot().findActor(ScreenComponent.Back.name());
      if (backButton != null) // TODO: required only for level editor - why?
      stage.getRoot().addActor(backButton);
      // Add guru before modelcontrols so that controls are accessible.
      stage.getRoot().addActorBefore(modelControls, guru);
    }
    return guru;
  }
  
  @Override
  public Actor addScience2DActor(String type, String viewSpec,  float x, float y, float rotation) {
    Science2DBody science2DBody = 
        getModel().addBody(type, x / ScreenComponent.PIXELS_PER_M, 
            y / ScreenComponent.PIXELS_PER_M, 
            rotation * MathUtils.degreesToRadians);
    Actor actor = createActor(type, viewSpec, science2DBody);
    
    if (actor == null && type.equals("ModelControls")) {
      return getView().findActor(type);
    }
    if (actor == null) return null;
    
    Stage stage = (Stage) science2DView;
    stage.addActor(actor);
    return actor;
  }
  
  // Factory method for creating science2D actors
  protected Actor createActor(String type, String viewSpec, Science2DBody body) {
    ComponentType componentType;
    try {
      componentType = ComponentType.valueOf(type);
    } catch(IllegalArgumentException e) {
      return null;
    }
    
    switch (componentType) {
    case Dummy:
    case Environment:
      Pixmap pixmap = new Pixmap(8, 8, Format.RGBA8888);
      pixmap.setColor(Color.LIGHT_GRAY);
      pixmap.fillRectangle(0, 0, 8, 8);
      TextureRegion textureRegion = new TextureRegion(new Texture(pixmap));
      pixmap.dispose();
      Science2DActor science2DActor = new Science2DActor(body, textureRegion);
      science2DActor.setPositionFromViewCoords(false);
      return science2DActor;      
    case Image:
      Actor actor = new Image(ScienceEngine.assetManager.get(viewSpec, Texture.class));
      actor.setName(viewSpec);
      return actor;
    }
    return null;
  }
  
  @Override
  public AbstractTutor createTutor(ITutor parent, String type, String goal,
      Array<?> components, Array<?> configs, int deltaSuccessScore, int deltaFailureScore, String[] hints) {
    if ("ParameterProber".equals(type)) {
      return new ParameterProber(this, parent, goal, components, configs, deltaSuccessScore, deltaFailureScore, hints);
    } else if ("TutorGroup".equals(type)) {
      return new TutorGroup(this, parent, goal, components, configs, deltaSuccessScore, deltaFailureScore, hints);
    } else if ("Subgoal".equals(type)) {
      return new Subgoal(this, parent, goal, components, configs, deltaSuccessScore, hints);
    }
    Gdx.app.error(ScienceEngine.LOG, "Could not load Tutor: " + type);
    return null;
  }

}
