package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.app.services.AsyncLevelLoader;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.ProfileManager;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.LevelUtil;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.view.IScience2DStage;
import com.mazalearn.scienceengine.experiments.electromagnetism.ElectroMagnetismController;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.ComponentType;
import com.mazalearn.scienceengine.experiments.molecules.StatesOfMatterController;
import com.mazalearn.scienceengine.experiments.waves.WaveController;

/**
 * Experiment screen corresponding to one level of the experiment.
 */
public class ExperimentScreen extends AbstractScreen {

  private IScience2DController science2DController;
  private Profile profile;
  private String experimentName;

  public ExperimentScreen(ScienceEngine scienceEngine, 
      int level, String experimentName) {
    super(scienceEngine, null);
    this.experimentName = experimentName;
    String fileName = LevelUtil.getLevelFilename(experimentName, ".json", level);
    if (ScienceEngine.assetManager.isLoaded(fileName)) {
      this.science2DController = ScienceEngine.assetManager.get(fileName);
    } else {
      this.science2DController = 
          createExperimentController(experimentName, level, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
    }
    IScience2DStage science2DStage = science2DController.getView();
    ProfileManager profileManager = ScienceEngine.getProfileManager();
    profile = profileManager.retrieveProfile();
    profile.setCurrentLevel(level);
    if (ScienceEngine.DEV_MODE == DevMode.DESIGN) {
      Stage levelEditor = 
          ScienceEngine.getPlatformAdapter().createLevelEditor(science2DController, this);
      this.setStage(levelEditor);
    } else {
      this.setStage((Stage) science2DStage);
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
    Gdx.graphics.setContinuousRendering(true);
  }

  @Override
  public void show() {
    super.show();
  }
  
  @Override
  protected void goBack() {
    ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
    ExperimentHomeScreen experimentHomeScreen = 
        new ExperimentHomeScreen(scienceEngine, experimentName);
    scienceEngine.setScreen(new LoadingScreen(scienceEngine, experimentHomeScreen));
    profile.setCurrentLevel(0);
  }
  
  public IScience2DController createExperimentController(
      String experimentName, int level, int width, int height) {
    if (experimentName.equalsIgnoreCase(StatesOfMatterController.NAME)) {
      return new StatesOfMatterController(level, width, height, getSkin());
    } else if (experimentName.equalsIgnoreCase(WaveController.NAME)) {
      return  new WaveController(level, width, height, getAtlas(), getSkin());
    } else if (experimentName.equalsIgnoreCase(ElectroMagnetismController.NAME)) {
      return new ElectroMagnetismController(level, width, height, getSkin());
    }
    return null;
  }
  
  @Override
  public void addAssets() {
    String fileName = LevelUtil.getLevelFilename(science2DController.getName(), ".json", science2DController.getLevel());
    if (ScienceEngine.assetManager.isLoaded(fileName)) {
      return;
    }
    ScienceEngine.assetManager.load("images/coppercoils-back.png", Texture.class);
    ScienceEngine.assetManager.load("images/brush.png", Texture.class);
    
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
