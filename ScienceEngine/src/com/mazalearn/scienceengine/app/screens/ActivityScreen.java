package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.loaders.AsyncLevelLoader;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;
import com.mazalearn.scienceengine.app.utils.LevelUtil;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.domains.electromagnetism.ElectroMagnetismController;
import com.mazalearn.scienceengine.domains.electromagnetism.model.ComponentType;
import com.mazalearn.scienceengine.domains.statesofmatter.StatesOfMatterController;
import com.mazalearn.scienceengine.domains.waves.WaveController;

/**
 * Activity screen corresponding to one level.
 */
public class ActivityScreen extends AbstractScreen {

  private IScience2DController science2DController;
  private Profile profile;
  private Topic topic;
  @SuppressWarnings("unused")
  private int activityLevel;

  public ActivityScreen(ScienceEngine scienceEngine, Topic topic, int activityLevel) {
    super(scienceEngine, null);
    this.topic = topic;
    this.activityLevel = activityLevel;
    String fileName = LevelUtil.getLevelFilename(topic.name(), ".json", activityLevel);
    if (ScienceEngine.assetManager.isLoaded(fileName)) {
      ScienceEngine.assetManager.unload(fileName);
    }
    this.science2DController = 
        createTopicController(topic, activityLevel, ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT);
    IScience2DView science2DView = science2DController.getView();
    profile = ScienceEngine.getPreferencesManager().getProfile();
    profile.setCurrentActivity(activityLevel);
    if (ScienceEngine.DEV_MODE == DevMode.DESIGN) {
      Stage levelEditor = 
          ScienceEngine.getPlatformAdapter().createLevelEditor(science2DController, this);
      this.setStage(levelEditor);
    } else {
      this.setStage((Stage) science2DView);
    }
    stage.addListener(new InputListener() {
      @Override
      public boolean keyDown(InputEvent event, int keycode) {
        if (keycode == Keys.BACK) {
          goBack();
          return true;
        }
        return super.keyDown(event, keycode);
      }      
    });
    setTitle(science2DController.getTitle());
    if (ScienceEngine.getPlatformAdapter().getPlatform() != IPlatformAdapter.Platform.GWT) {
      Gdx.graphics.setContinuousRendering(true);
    }
  }

  @Override
  protected void goBack() {
    // Stop tutoring if it was in progress
    science2DController.getGuru().endTutoring();
    TopicHomeScreen topicHomeScreen = 
        new TopicHomeScreen(scienceEngine, topic);
    profile.setCurrentActivity(0);
    scienceEngine.setScreen(new LoadingScreen(scienceEngine, topicHomeScreen));
  }
  
  public IScience2DController createTopicController(
      Topic topic, int level, int width, int height) {
    switch (topic) {
    case StatesOfMatter: 
      return new StatesOfMatterController(level, width, height, getSkin());
    case Waves:
      return  new WaveController(level, width, height, getSkin());
    case Electromagnetism:
      return new ElectroMagnetismController(level, width, height, getSkin());
    }
    return null;
  }
  
  @Override
  public void addAssets() {
    String fileName = LevelUtil.getLevelFilename(topic.name(), 
        ".json", science2DController.getLevel());
    if (ScienceEngine.assetManager.isLoaded(fileName)) {
      return;
    }
    // Guru resources
    ScienceEngine.assetManager.load("images/greenballoon.png", Texture.class);
    ScienceEngine.assetManager.load("images/redballoon.png", Texture.class);
    ScienceEngine.assetManager.load("images/check.png", Texture.class);
    ScienceEngine.assetManager.load("images/cross.png", Texture.class);
    
    // TODO: Move assets inside appropriate view - automatically infer if possile.
    // TODO: Load corresponding atlas and use
    // ScienceEngine.assetManager.load("images/electromagnetism/pack.atlas", TextureAtlas.class);
    ScienceEngine.assetManager.load("images/coppercoils-back.png", Texture.class);
    ScienceEngine.assetManager.load("images/coppercoils-front2.png", Texture.class);
    ScienceEngine.assetManager.load("images/brush.png", Texture.class);
    ScienceEngine.assetManager.load("images/engine.png", Texture.class);
    ScienceEngine.assetManager.load("images/wheel.png", Texture.class);
    
    for (ComponentType componentType: ComponentType.values()) {
      String textureFilename = componentType.getTextureFilename();
      if (textureFilename != null && !textureFilename.equals("")) {
        ScienceEngine.assetManager.load(textureFilename, Texture.class);
      }
    }
    AsyncLevelLoader.LevelLoaderParameter parameter = new AsyncLevelLoader.LevelLoaderParameter();
    parameter.science2DController = science2DController;
    ScienceEngine.assetManager.load(fileName, IScience2DController.class, parameter);
  }

}
