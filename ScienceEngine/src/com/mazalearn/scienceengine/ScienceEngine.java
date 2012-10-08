package com.mazalearn.scienceengine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.app.screens.ExperimentScreen;
import com.mazalearn.scienceengine.app.screens.SplashScreen;
import com.mazalearn.scienceengine.app.services.LevelManager;
import com.mazalearn.scienceengine.app.services.Messages;
import com.mazalearn.scienceengine.app.services.MusicManager;
import com.mazalearn.scienceengine.app.services.PreferencesManager;
import com.mazalearn.scienceengine.app.services.ProfileManager;
import com.mazalearn.scienceengine.app.services.SoundManager;
import com.mazalearn.scienceengine.app.utils.PlatformAdapter;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.view.IScience2DStage;
import com.mazalearn.scienceengine.experiments.electromagnetism.ElectroMagnetismController;

public class ScienceEngine extends Game {
  // constant useful for logging
  public static final String LOG = ScienceEngine.class.getName();

  // mode of development
  public enum DevMode {PRODUCTION, DEBUG, DESIGN};
  public static DevMode DEV_MODE = DevMode.DEBUG;
  
  // Provide access to this singleton scienceEngine from any class
  public static ScienceEngine SCIENCE_ENGINE;

  // a libgdx helper class that logs the current FPS each second
  private FPSLogger fpsLogger;

  // services
  private static PreferencesManager preferencesManager;
  private static ProfileManager profileManager;
  private static MusicManager musicManager;
  private static SoundManager soundManager;
  private PlatformAdapter platformAdapter;

  private List<String> params;

  private static Skin skin;

  public static final int PIXELS_PER_M = 8;

  public ScienceEngine() {
    this(new ArrayList<String>());
  }
  
  public ScienceEngine(List<String> intentParams) {
    // Ignored for now - later should support auto launch from browser link
    this.params = intentParams;
  }

  public static PreferencesManager getPreferencesManager() {
    return preferencesManager;
  }

  public static ProfileManager getProfileManager() {
    return profileManager;
  }

  public static MusicManager getMusicManager() {
    return musicManager;
  }

  public static SoundManager getSoundManager() {
    return soundManager;
  }

  public Skin getSkin() {
    if (skin == null) {
      FileHandle skinFile = Gdx.files.internal("skin/uiskin.json");
      skin = new Skin(skinFile);
      skin.add("en", skin.getFont("default-font"));
      Messages.setLocale(skin, new Locale("en"), getPlatform());
   }
    return skin;
  }
  
  // PlatformAdapter interface
  
  public void setUrlViewer(PlatformAdapter platformAdapter) {
    this.platformAdapter = platformAdapter;
  }
  
  public void browseURL(String url){
    if (platformAdapter != null) {
      platformAdapter.browseURL(url);
    }
  }


  public boolean playVideo(File file) {
    if (platformAdapter != null) {
      return platformAdapter.playVideo(file);
    }
    return false;
  }
  // Game-related methods
  
  public PlatformAdapter.Platform getPlatform() {
    return platformAdapter.getPlatform();
  }

  @Override
  public void create() {
    Gdx.app.log(ScienceEngine.LOG, "Creating Engine on " + Gdx.app.getType());
    Gdx.app.log(ScienceEngine.LOG, "With params " + params);
    // Resize to full screen
    //Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode());

    // create the preferences manager
    preferencesManager = new PreferencesManager();

    // create the music manager
    musicManager = new MusicManager();
    musicManager.setVolume(preferencesManager.getVolume());
    musicManager.setEnabled(preferencesManager.isMusicEnabled());

    // create the sound manager
    soundManager = new SoundManager();
    soundManager.setVolume(preferencesManager.getVolume());
    soundManager.setEnabled(preferencesManager.isSoundEnabled());

    // create the profile manager
    profileManager = new ProfileManager();
    profileManager.retrieveProfile();

    if (DEV_MODE != DevMode.PRODUCTION) {
      // create the helper objects
      fpsLogger = new FPSLogger();
    }
    
    SCIENCE_ENGINE = this;
  }

  @Override
  public void resize(int width, int height) {
    super.resize(width, height);
    Gdx.app.log(ScienceEngine.LOG, "Resizing engine to: " + width + " x "
        + height);

    // show the starting screen when the scienceEngine is resized for the first time;
    // this approach avoids calling the screen's resize method repeatedly
    if (getScreen() == null) {
      setScreen(createStartScreen());
    }
  }

  private AbstractScreen createStartScreen() {
    // Look through params to identify right screen
    // params come as pathsegments
    // e.g. science/karnataka/electromagnetism?t=20
    // TODO: Later handle first 2 path segments identifying subject and board
    if (params.size() >= 4) {
      String topic = params.get(2);
      String level = params.get(3);
      int iLevel = (level.equals("t=20")) ? 4 : 1;
      if (topic.equals("electromagnetism")) {
        IScience2DController science2DController = 
            new ElectroMagnetismController(AbstractScreen.VIEWPORT_WIDTH, AbstractScreen.VIEWPORT_HEIGHT, getSkin());
        final IScience2DStage science2DStage = science2DController.getView();
        LevelManager levelManager = science2DStage.getLevelManager();
        levelManager.setLevel(iLevel);
  
        return new ExperimentScreen(this, iLevel, science2DController);
      }
    }
    return new SplashScreen(this);
  }

  @Override
  public void render() {
    super.render();
    // output the current FPS
    if (DEV_MODE != DevMode.PRODUCTION) {
      // fpsLogger.log();
    }
  }

  @Override
  public void dispose() {
    super.dispose();
    Gdx.app.log(ScienceEngine.LOG, "Disposing engine");

    // dispose some services
    musicManager.dispose();
    soundManager.dispose();
  }

  @Override
  public void pause() {
    super.pause();
    Gdx.app.log(ScienceEngine.LOG, "Pausing engine");

    // persist the profile, because we don't know if the player will come
    // back to the scienceEngine
    profileManager.persist();
  }

  @Override
  public void resume() {
    super.resume();
    Gdx.app.log(ScienceEngine.LOG, "Resuming engine");
  }

  @Override
  public void setScreen(Screen screen) {
    super.setScreen(screen);
    Gdx.app.log(ScienceEngine.LOG, "Setting screen: "
        + screen.getClass().getName());
  }

  public List<String> getParams() {
    return params;
  }
}