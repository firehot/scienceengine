package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
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
import com.mazalearn.scienceengine.core.view.Science2DGestureDetector;
import com.mazalearn.scienceengine.domains.electromagnetism.ElectroMagnetismController;
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
  private Topic activityLevel;

  public ActivityScreen(ScienceEngine scienceEngine, Topic topic, Topic level) {
    super(scienceEngine, null);
    this.topic = topic;
    this.activityLevel = level;
    String fileName = LevelUtil.getLevelFilename(topic, level, ".json");
    if (ScienceEngine.getAssetManager().isLoaded(fileName)) {
      ScienceEngine.getAssetManager().unload(fileName);
    }
    this.science2DController = 
        createTopicController(topic, level, ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT);
    IScience2DView science2DView = science2DController.getView();
    profile = ScienceEngine.getPreferencesManager().getProfile();
    profile.setCurrentActivity(level);
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
  
  public void show() {
    super.show();
    IScience2DView science2DView = science2DController.getView();
    InputProcessor gestureListener = new Science2DGestureDetector((Stage) science2DView);
    Gdx.input.setInputProcessor(new InputMultiplexer(gestureListener, (Stage) science2DView));
    Gdx.app.log(ScienceEngine.LOG, "Set gesture detector");
  }
  
  @Override
  protected void goBack() {
    // Stop tutoring if it was in progress
    science2DController.getView().tutoring(false);
    TopicHomeScreen topicHomeScreen = 
        new TopicHomeScreen(scienceEngine, topic);
    profile.setCurrentActivity(null);
    scienceEngine.setScreen(new LoadingScreen(scienceEngine, topicHomeScreen));
  }
  
  public IScience2DController createTopicController(
      Topic topic, Topic level, int width, int height) {
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
    String fileName = LevelUtil.getLevelFilename(topic, 
        science2DController.getLevel(), ".json");
    if (ScienceEngine.getAssetManager().isLoaded(fileName)) {
      return;
    }
    // Guru resources
    ScienceEngine.loadAtlas("images/guru/pack.atlas");
    // Topic resources
    ScienceEngine.loadAtlas("images/" + topic.name() + "/pack.atlas");
    AsyncLevelLoader.LevelLoaderParameter parameter = new AsyncLevelLoader.LevelLoaderParameter();
    parameter.science2DController = science2DController;
    ScienceEngine.getAssetManager().load(fileName, IScience2DController.class, parameter);
  }

}
