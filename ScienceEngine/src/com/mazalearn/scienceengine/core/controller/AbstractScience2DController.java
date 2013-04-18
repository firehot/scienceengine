package com.mazalearn.scienceengine.core.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.services.loaders.LevelLoader;
import com.mazalearn.scienceengine.core.model.ComponentType;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.AbstractScience2DView;
import com.mazalearn.scienceengine.core.view.AnimateAction;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.core.view.ModelControls;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.core.view.ViewControls;
import com.mazalearn.scienceengine.tutor.AbstractTutor;
import com.mazalearn.scienceengine.tutor.Abstractor;
import com.mazalearn.scienceengine.tutor.Guru;
import com.mazalearn.scienceengine.tutor.ITutor;
import com.mazalearn.scienceengine.tutor.KnowledgeUnit;
import com.mazalearn.scienceengine.tutor.McqTutor;
import com.mazalearn.scienceengine.tutor.ParameterProber;
import com.mazalearn.scienceengine.tutor.TutorGroup;
import com.mazalearn.scienceengine.tutor.TutorType;

public abstract class AbstractScience2DController implements
    IScience2DController {

  protected ModelControls modelControls;
  protected ViewControls viewControls;
  protected IScience2DModel science2DModel;
  protected IScience2DView science2DView;
  protected Skin skin;
  private Topic topic;
  private Topic level;
  private Guru guru;

  protected AbstractScience2DController(Topic topic, Topic level, Skin skin) {
    this.topic = topic;
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
  public Topic getTopic() {
    return topic;
  }
  
  @Override
  public String getTitle() {
    return ScienceEngine.getMsg().getString(topic + "." + level + ".Name");    
  }
  
  @Override
  public Topic getLevel() {
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
    // TODO: tutorHelper should also be reset
    // getGuru().teach();
  }

  @Override
  public Guru getGuru() {
    if (guru == null) {
      Stage stage = (Stage) science2DView;
      guru = new Guru(skin, this, this.getTitle());
      // Bring basic Screen to top.
      // Three layers - components, tutor, core.
      Actor coreGroup = stage.getRoot().findActor(ScreenComponent.CORE_GROUP);
      stage.getRoot().addActorBefore(coreGroup, guru);
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
    
    Group activityGroup = (Group) science2DView.findActor(ScreenComponent.ACTIVITY_GROUP);
    activityGroup.addActor(actor);
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
      Science2DActor science2DActor = new Science2DActor(body, textureRegion) {
        @Override
        public void showHelp(Stage stage, boolean animate) {
          Group activityGroup = (Group) science2DView.findActor(ScreenComponent.ACTIVITY_GROUP);
          if (animate) {
            activityGroup.addAction(AnimateAction.animatePosition(getX(), getY()));
          } else {
            activityGroup.clearActions();
          }
        }
      };
      science2DActor.setPositionFromViewCoords(false);
      return science2DActor;      
    case Image:
      Actor actor = new Image(ScienceEngine.getTextureRegion(viewSpec));
      actor.setName(viewSpec);
      return actor;
    default:
      return null;
    }
  }
  
  @Override
  public AbstractTutor createTutor(ITutor parent, String type, String goal, String id,
      Array<?> components, Array<?> configs, String[] hints,
      String[] explanation, String[] refs) {
    TutorType tutorType;
    try {
      tutorType = TutorType.valueOf(type);
    } catch(IllegalArgumentException e) {
      Gdx.app.error(ScienceEngine.LOG, "Could not recognize Tutor: " + type);
      return null;
    }
    switch (tutorType) {
    case MCQ1:
      return new McqTutor(this, tutorType, parent, goal, id, components, configs, skin, 
          hints, explanation, refs, true);
    case MCQ:
      return new McqTutor(this, tutorType, parent, goal, id, components, configs, skin, 
          hints, explanation, refs, false);
    case ParameterProber:
      return new ParameterProber(this, tutorType, parent, goal, id, components, configs, 
          hints, explanation, refs);
    case Challenge:
    case RapidFire:
    case Guide:
    case Reviewer:
      return new TutorGroup(this, tutorType, parent, goal, id, components, configs, 
          hints, explanation, refs);
    case KnowledgeUnit:
      return new KnowledgeUnit(this, tutorType, parent, goal, id, components, configs, 
          hints, explanation, refs);
    case Abstractor:
      return new Abstractor(this, tutorType, parent, goal, id, components, configs, skin, 
          science2DView.getModelControls(), hints, explanation, refs);
    default:
      Gdx.app.error(ScienceEngine.LOG, "Could not create Tutor: " + type);
      return null;
    }
  }

}
