package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.loaders.LevelLoader;
import com.mazalearn.scienceengine.core.model.ComponentType;
import com.mazalearn.scienceengine.core.model.IComponentType;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.AbstractScience2DView;
import com.mazalearn.scienceengine.core.view.ControlPanel;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.guru.AbstractTutor;
import com.mazalearn.scienceengine.guru.Guide;
import com.mazalearn.scienceengine.guru.Guru;
import com.mazalearn.scienceengine.guru.ParameterProber;

public abstract class AbstractScience2DController implements
    IScience2DController {

  protected ControlPanel controlPanel;
  protected IScience2DModel science2DModel;
  protected AbstractScience2DView science2DView;
  protected Skin skin;
  private String name;
  private int level;
  private Guru guru;

  protected AbstractScience2DController(String name, int level, Skin skin) {
    this.name = name;
    this.level = level;
    this.skin = skin;
  }
  
  protected void initialize(IScience2DModel science2DModel, 
      AbstractScience2DView science2DView) {
    this.science2DModel = science2DModel;
    this.science2DView = science2DView;
    this.controlPanel = new ControlPanel(this, this.getName(), skin);
    science2DView.setControlPanel(this.controlPanel);
  }
  
  @Override
  public String getName() {
    return name;
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
  public ControlPanel getControlPanel() {
    return controlPanel;
  }
  
  @Override
  public void reload() {
    new LevelLoader(this).reload();
  }

  @Override
  public Guru getGuru() {
    if (guru == null) {
      Stage stage = (Stage) getView();
      guru = new Guru(skin, stage.getWidth(), stage.getHeight(), this, controlPanel);
      // Move control Panel to top - so it will be above others
      stage.getRoot().addActor(controlPanel);
      // Move back button to top also - so it will be accessible
      stage.getRoot().addActor(stage.getRoot().findActor("BackButton"));
      // Add guru before controlpanel so that controls are accessible.
      stage.getRoot().addActorBefore(controlPanel, guru);
    }
    return guru;
  }
  
  @Override
  public Actor addScience2DActor(String type, String viewSpec,  float x, float y, float rotation) {
    Science2DBody science2DBody = 
        getModel().addBody(type, x / ScienceEngine.PIXELS_PER_M, 
            y / ScienceEngine.PIXELS_PER_M, 
            rotation * MathUtils.degreesToRadians);
    Actor actor = createActor(type, viewSpec, science2DBody);
    if (actor == null && type.equals("ControlPanel")) {
      actor = getView().findActor(type);
    }
    if (actor == null) return null;
    
    science2DView.addActor(actor);
    return actor;
  }
  
  // Factory method for creating science2D actors
  protected Actor createActor(String type, String viewSpec, Science2DBody body) {
    IComponentType componentType;
    try {
      componentType = ComponentType.valueOf(type);
    } catch(IllegalArgumentException e) {
      return null;
    }
    
    if (componentType == ComponentType.Dummy || componentType == ComponentType.Environment) {
      Pixmap pixmap = new Pixmap(8, 8, Format.RGBA8888);
      pixmap.setColor(Color.LIGHT_GRAY);
      pixmap.fillRectangle(0, 0, 8, 8);
      TextureRegion textureRegion = new TextureRegion(new Texture(pixmap));
      pixmap.dispose();
      Science2DActor science2DActor = new Science2DActor(body, textureRegion);
      science2DActor.setPositionFromViewCoords(false);
      return science2DActor;      
    } else if (componentType == ComponentType.Image) {
      Actor actor = new Image(ScienceEngine.assetManager.get(viewSpec, Texture.class));
      actor.setName(viewSpec);
      return actor;
    }
    return null;
  }
  
  @Override
  public AbstractTutor createTutor(String type, String goal,
      Array<?> components, Array<?> configs, int deltaSuccessScore, int deltaFailureScore) {
    if ("ParameterProber".equals(type)) {
      return new ParameterProber(this, goal, components, configs, deltaSuccessScore, deltaFailureScore);
    } else if ("Guide".equals(type)) {
      return new Guide(this, goal, components, configs, deltaSuccessScore, deltaFailureScore);
    }
    return null;
  }

}
