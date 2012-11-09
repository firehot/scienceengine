package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.app.services.LevelLoader;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.ProfileManager;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
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
    this.science2DController = 
        createExperimentController(experimentName, level, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
    IScience2DStage science2DStage = science2DController.getView();
    ProfileManager profileManager = ScienceEngine.getProfileManager();
    profile = profileManager.retrieveProfile();
    profile.setCurrentLevel(level);
    load(level, science2DController);
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

  /**
   * Loads the content of the provided file and automatically position and size
   * the objects.
   * @param index 
   */
  private void load(int level, IScience2DController science2DController) {
    try {
      LevelLoader levelLoader = new LevelLoader(science2DController);
      levelLoader.load();
      Gdx.app.log(ScienceEngine.LOG, "[LevelEditor] Level successfully loaded!");
    } catch (GdxRuntimeException ex) {
      System.err.println("[LevelEditor] Error happened while loading level");
    }
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
    for (ComponentType componentType: ComponentType.values()) {
      String textureFilename = componentType.getTextureFilename();
      if (textureFilename != null && !textureFilename.equals("")) {
        ScienceEngine.assetManager.load(textureFilename, Texture.class);
      }
    }    
  }

}
