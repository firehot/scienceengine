package com.mazalearn.scienceengine;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.app.screens.ExperimentHomeScreen;
import com.mazalearn.scienceengine.app.screens.ExperimentScreen;
import com.mazalearn.scienceengine.app.screens.SplashScreen;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.app.services.LevelManager;
import com.mazalearn.scienceengine.app.services.MusicManager;
import com.mazalearn.scienceengine.app.services.PreferencesManager;
import com.mazalearn.scienceengine.app.services.ProfileManager;
import com.mazalearn.scienceengine.app.services.SoundManager;
import com.mazalearn.scienceengine.app.utils.PlatformAdapter;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.view.IScience2DStage;
import com.mazalearn.scienceengine.experiments.electromagnetism.ElectroMagnetismController;
import com.mazalearn.scienceengine.experiments.molecules.StatesOfMatterController;
import com.mazalearn.scienceengine.experiments.waves.WaveController;

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
  private static PlatformAdapter platformAdapter;
  private static Skin skin;

  private String uri;

  private TextureAtlas atlas;


  public static final int PIXELS_PER_M = 8;

  public ScienceEngine() {
    this(null);
  }
  
  public ScienceEngine(String url) {
    if (url == null) return;
    this.uri = url;
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
      getMsg().setLanguage(skin, "en");
   }
    return skin;
  }
  
  public TextureAtlas getAtlas() {
    if (atlas == null) {
      atlas = new TextureAtlas(Gdx.files.internal("image-atlases/pages.atlas")); //$NON-NLS-1$
    }
    return atlas;
  }

  // PlatformAdapter interface
  
  public void setPlatformAdapter(PlatformAdapter platformAdapter) {
    ScienceEngine.platformAdapter = platformAdapter;
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
  
  @Override
  public void create() {
    Gdx.app.log(ScienceEngine.LOG, "Creating Engine on " + Gdx.app.getType());
    Gdx.app.log(ScienceEngine.LOG, "With params " + uri);
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
      setScreen(createScreen(uri));
    }
  }

  /**
   * Process the uri to identify right screen
   * e.g. science/karnataka/electromagnetism?level=2
   * regular expression is subject?board?topic(\?"t="{0-9})?
   * this should all be in lower case
   * subject = science
   * board = karnataka | tamilnadu | cbse
   * topic = any supported topic - electromagnetism | waves | statesofmatter
   * @param uri
   * @return screen corresponding to uri
   */
  private AbstractScreen createScreen(String uri) {
    if (uri != null) {
      String path, query;
      int qmark = uri.indexOf("?");
      if (qmark == -1) {
        path = uri;
        query = null;
      } else {
        path = uri.substring(0, qmark);
        query = uri.substring(qmark);
      }
      String[] pathSegments = path.toLowerCase().split("/");
      int i = 1;
      if (pathSegments.length > i && isSupportedSubject(pathSegments[i])) i++;
      if (pathSegments.length > i && isSupportedBoard(pathSegments[i])) i++;
      if (pathSegments.length > i && isSupportedTopic(pathSegments[i])) {
        IScience2DController science2DController = 
            createExperimentController(pathSegments[i], 
                AbstractScreen.VIEWPORT_WIDTH, 
                AbstractScreen.VIEWPORT_HEIGHT);
        Integer iLevel = null;
        if (query != null) {
          String[] queryParts = query.toLowerCase().split("&");
          if (queryParts[0].startsWith("level")) {
            iLevel = Integer.parseInt(queryParts[0].substring("level=".length()));
          }
        }
        if (iLevel == null) {
          return new ExperimentHomeScreen(this, science2DController);
        }
        final IScience2DStage science2DStage = science2DController.getView();
        LevelManager levelManager = science2DStage.getLevelManager();
        levelManager.setLevel(iLevel);    
        return new ExperimentScreen(this, iLevel, science2DController);
      }
    }
    return new SplashScreen(this);
  }

  private boolean isSupportedSubject(String token) {
    List<String> subjects = 
        Arrays.asList(new String[] {"science"});
    return subjects.contains(token);
  }
  
  private boolean isSupportedBoard(String token) {
    List<String> boards = 
        Arrays.asList(new String[] {"karnataka", "tamilnadu", "cbse"});
    return boards.contains(token);
  }
  
  private boolean isSupportedTopic(String token) {
    List<String> topics = 
        Arrays.asList(new String[] {"electromagnetism", "waves", "statesofmatter"});
    return topics.contains(token);
  }
  
  public IScience2DController createExperimentController(
      String experimentName, int width, int height) {
    if (experimentName.equalsIgnoreCase(StatesOfMatterController.NAME)) {
      return new StatesOfMatterController(width, height, getSkin());
    } else if (experimentName.equalsIgnoreCase(WaveController.NAME)) {
      return  new WaveController(width, height, getAtlas(), getSkin());
    } else if (experimentName.equalsIgnoreCase(ElectroMagnetismController.NAME)) {
      return new ElectroMagnetismController(width, height, getSkin());
    }
    return null;
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
    if (skin != null)
      skin.dispose();
    if (atlas != null)
      atlas.dispose();
  }

  @Override
  public void pause() {
    super.pause();
    Gdx.app.log(ScienceEngine.LOG, "Pausing engine");

    // persist the profile, because we don't know if the player will come
    // back to the scienceEngine
    profileManager.persist();
    // For some reason, skin and atlas do not survive pause
    skin = null;
    atlas = null;
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

  public static PlatformAdapter getPlatformAdapter() {
    return platformAdapter;  
  }

  public static IMessage getMsg() {
    return platformAdapter.getMsg();
  }
}