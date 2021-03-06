package com.mazalearn.scienceengine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mazalearn.scienceengine.app.dialogs.AppRater;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.app.screens.ActivityScreen;
import com.mazalearn.scienceengine.app.screens.LoadingScreen;
import com.mazalearn.scienceengine.app.screens.SplashScreen;
import com.mazalearn.scienceengine.app.screens.TopicHomeScreen;
import com.mazalearn.scienceengine.app.services.EventLog;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.app.services.MusicManager;
import com.mazalearn.scienceengine.app.services.PreferencesManager;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.SoundManager;
import com.mazalearn.scienceengine.app.services.loaders.AsyncLevelLoader;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.IParameter;
import com.mazalearn.scienceengine.core.model.Parameter;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.IScience2DView;

public class ScienceEngine extends Game {
  public static final String USER = "user";

  // constant useful for logging
  public static final String LOG = ScienceEngine.class.getName();

  public static DevMode DEV_MODE = new DevMode();
  
  // Provide access to this singleton scienceEngine from any class
  public static ScienceEngine SCIENCE_ENGINE;

  // a libgdx helper class that logs the current FPS each second
  private FPSLogger fpsLogger;

  // base of skin being used
  private static String SKIN_BASE = "skin/uiskin1";
  
  // services
  private static PreferencesManager preferencesManager;
  private static MusicManager musicManager;
  private static SoundManager soundManager;
  private static IPlatformAdapter platformAdapter;
  private static AssetManager assetManager;
  private static Skin skin;

  private String uri;

  private static Map<String, TextureAtlas> atlasMap = new HashMap<String, TextureAtlas>();


  private static Science2DBody selectedBody;

  private static boolean isProbeMode;

  private static EventLog eventLog = new EventLog();

  private static float time;

  private static Set<Science2DBody> pinnedBodies = new HashSet<Science2DBody>();

  private static long logicalTime;

  private Device device;

  public ScienceEngine(String url, Device device) {
    this.uri = url;
    this.device = device;
  }

  public static PreferencesManager getPreferencesManager() {
    // TODO: cleanup - below initialization added for testing
    if (preferencesManager == null) {
      preferencesManager = new PreferencesManager(getPlatformAdapter());
    }
    return preferencesManager;
  }

  public static MusicManager getMusicManager() {
    return musicManager;
  }

  public static SoundManager getSoundManager() {
    return soundManager;
  }

  public static Skin getSkin() {
    if (skin == null) {
      FileHandle skinFile = Gdx.files.internal(SKIN_BASE + ".json");
      skin = new Skin(skinFile, new TextureAtlas(Gdx.files.internal(SKIN_BASE + ".atlas")));
      skin.add("en", skin.getFont(ScreenComponent.getFont(1)));
      skin.add("en-small", skin.getFont(ScreenComponent.getFont(0.80f)));
      skin.add("en-big", skin.getFont(ScreenComponent.getFont(1.5f)));
      getMsg().setFont(skin);
      skin.add("en", skin.getFont("default-font"));
      getMsg().setLanguage(skin, "en");
   }
    return skin;
  }
  
  // IPlatformAdapter interface
  
  public static void setPlatformAdapter(IPlatformAdapter platformAdapter) {
    ScienceEngine.platformAdapter = platformAdapter;
  }
  
  // Game-related methods
  
  @Override
  public void create() {
    Gdx.app.log(ScienceEngine.LOG, "Creating Engine on " + Gdx.app.getType());
    Gdx.app.log(ScienceEngine.LOG, "With params " + uri);

    // create the preferences Manager
    preferencesManager = new PreferencesManager(getPlatformAdapter());

    try {
      // create the music Manager
      musicManager = new MusicManager();
      musicManager.setVolume(preferencesManager.getVolume());
      musicManager.setEnabled(preferencesManager.isMusicEnabled());
  
      // create the sound Manager
      soundManager = new SoundManager();
      soundManager.setVolume(preferencesManager.getVolume());
      soundManager.setEnabled(preferencesManager.isSoundEnabled());
    } catch (RuntimeException e) {
      // ignore - not having sound is OK. Added for GWT.
    }

    // create the asset Manager
    assetManager = new AssetManager();
    getAssetManager().setLoader(IScience2DController.class, 
        new AsyncLevelLoader(new InternalFileHandleResolver()));
    loadAtlas("images/core/pack.atlas");

    //if (DEV_MODE != DevMode.PRODUCTION) {
      // create the helper objects
      fpsLogger = new FPSLogger();
    //}
      
    Dialog.fadeDuration = 0; // Set preference for dialogs not to fade in/out
    
    if (ScienceEngine.DEV_MODE.isDebug()) {
      ScreenComponent.setSize(device.width, device.height);
    } else {
      DisplayMode displayMode = Gdx.graphics.getDesktopDisplayMode();
      ScreenComponent.setSize(displayMode.width, displayMode.height);
    }
    
    resize(ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT);
    SCIENCE_ENGINE = this;
  }

  @Override
  public void resize(int width, int height) {
    super.resize(width, height);
    Gdx.app.log(ScienceEngine.LOG, "Resizing engine to: " + width + " x "
        + height);

    // show the starting screen when the scienceEngine is resized for the first timeLimit;
    // this approach avoids calling the screen's resize method repeatedly
    if (getScreen() == null) {
      setScreen(createScreen(uri));
      Stage stage = ((AbstractScreen) getScreen()).getStage();
      //AppRater.appLaunched(getSkin());
      AppRater.appLaunched(stage, getSkin());
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
    if (uri != null && getUserEmail().length() > 0) {
      String path, query;
      int qmark = uri.indexOf("?");
      if (qmark == -1) {
        path = uri;
        query = null;
      } else {
        path = uri.substring(0, qmark);
        query = uri.substring(qmark + 1);
      }
      String[] pathSegments = path.toLowerCase().split("/");
      int i = 3;
      if (pathSegments.length > i && isSupportedSubject(pathSegments[i])) i++;
      if (pathSegments.length > i && isSupportedBoard(pathSegments[i])) i++;
      if (pathSegments.length > i && findSupportedTopic(pathSegments[i]) != null) {
        Topic topic = findSupportedTopic(pathSegments[i]);
        Integer iLevel = null;
        if (query != null) {
          String[] queryParts = query.toLowerCase().split("&");
          if (queryParts[0].startsWith("level")) {
            iLevel = Integer.parseInt(queryParts[0].substring("level=".length()));
          }
        }
        if (iLevel != null && iLevel >= 0 && iLevel < topic.getChildren().length) {
          return new LoadingScreen(this, new ActivityScreen(this, topic, topic.getChildren()[iLevel]));
        }
        return new LoadingScreen(this, new TopicHomeScreen(this, topic));
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
  
  private Topic findSupportedTopic(String token) {
    for (Topic topic: Topic.values()) {
      if (topic.name().toLowerCase().equals(token)) return topic;
    }
    return null;
  }
  
  @Override
  public void render() {
    super.render();
    // output the current FPS
 //   if (DEV_MODE != DevMode.PRODUCTION) {
 //      fpsLogger.log();
 //   }
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
    if (atlasMap != null)
      for (TextureAtlas a: atlasMap.values())
        a.dispose();
  }

  @Override
  public void pause() {
    super.pause();
    Gdx.app.log(ScienceEngine.LOG, "Pausing engine");

    // For some reason, skin does not survive pause
    skin = null;
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

  public static IPlatformAdapter getPlatformAdapter() {
    return platformAdapter;  
  }

  public static IMessage getMsg() {
    return getPlatformAdapter().getMsg();
  }

  public static Science2DBody getSelectedBody() {
    return selectedBody;
  }

  /**
   * Body got selected. Record this event and display status.
   * @param body - not null
   * @param science2DView
   */
  public static void selectBody(Science2DBody body, IScience2DView science2DView) {
    selectedBody = body;
    if (body == null) return;
    eventLog.logEvent(body.name(), Parameter.Select.name());
    science2DView.checkActiveTutorProgress();
    displayEntityStatus(body, body.getComponentTypeName(), science2DView);
  }

  /**
   * Parameter got selected by user. Record this event and display status
   * @param parameter
   * @param science2DView
   */
  public static void selectParameter(Science2DBody body, IParameter parameter, float value, IScience2DView science2DView) {
    displayEntityStatus(body, parameter.name(), science2DView);
    if (body == null) return;
    science2DView.checkActiveTutorProgress();
    eventLog.logEvent(body.name(), parameter.name(), value);
  }
  
  public static void selectParameter(Science2DBody body, IParameter parameter, boolean value, IScience2DView stage) {
    selectParameter(body, parameter, value ? 1.0f : 0.0f, stage);
  }
  
  public static void selectParameter(Science2DBody body, IParameter parameter, String value, IScience2DView stage) {
    selectParameter(body, parameter, 0.0f, stage);
  }
  
  public static EventLog getEventLog() {
    return eventLog;
  }

  private static void displayEntityStatus(Science2DBody body, String entityName, IScience2DView stage) {
    if (stage == null) return;
    String component = body != null ? body.getLocalizedName() + " - " : "";
    if (entityName.contains(".")) {
      entityName = entityName.substring(0, entityName.indexOf("."));
    }
    String message = component + getMsg().getString("Help." + entityName);
    displayStatusMessage((Stage) stage, StatusType.INFO, message);
  }

  public static void displayStatusMessage(Stage stage, StatusType type, String message) {
    Label status = (Label) stage.getRoot().findActor(ScreenComponent.Status.name());
    if (status != null) {// TODO: only for level editor - why?
      status.setColor(type.color); 
      status.setText(message);
      Gdx.app.log(LOG, message);
    }
  }

  public static void setProbeMode(boolean probeMode) {
    isProbeMode = probeMode;
  }

  /**
   * Is activity in prober mode?
   * @return
   */
  public static boolean isProbeMode() {
    return isProbeMode;
  }

  /**
   * Keep track of actual time spent in application when it is active
   * @param delta
   */
  public synchronized static void addTimeElapsed(float delta) {
    time += delta;
    logicalTime++;
  }
  
  public static float getTime() {
    return time;
  }

  public static boolean isPinned(Science2DBody body) {
    return pinnedBodies.contains(body);
  }
  
  public static void pin(Science2DBody body, boolean pin) {
    if (pin) {
      pinnedBodies.add(body);
    } else {
      pinnedBodies.remove(body);
    }
  }

  public static void clearPins() {
    pinnedBodies.clear();
  }

  public static String getUserEmail() {
    return preferencesManager.getActiveUserProfile().getUserEmail();
  }

  public static String getUserName() {
    return preferencesManager.getActiveUserProfile().getUserName();
  }

  public static String getHostPort() {
    if (ScienceEngine.DEV_MODE.isDebug()) {
      return "localhost:8888";
    }
    return "www.mazalearn.com:80";
  }

  // This is the number of times act has been called on app as opposed to time elapsed
  public static long getLogicalTime() {
    return logicalTime;
  }
  
  public static void loadAtlas(String path) {
    getAssetManager().load(path, TextureAtlas.class);
    atlasMap.put(path, new TextureAtlas(Gdx.files.internal(path))); //$NON-NLS-1$
  }

  public static void unloadAtlas(String path) {
    atlasMap.remove(path);
    getAssetManager().unload(path);
  }

  public static TextureRegion getTextureRegion(String name) {
    if (USER.equals(name)) {
      Profile profile = ScienceEngine.getPreferencesManager().getActiveUserProfile();
      Pixmap userPixmap = profile.getUserPixmap();
      if (userPixmap != null) {
        TextureRegion textureRegion = new TextureRegion(new Texture(userPixmap));
        userPixmap.dispose();
        return textureRegion;
      }
    }
    for (TextureAtlas atlas: atlasMap.values()) {
      AtlasRegion textureRegion = atlas.findRegion(name);
      if (textureRegion != null) return textureRegion;
    }
    try {
      return new TextureRegion(new Texture("images/" + name + ".png"));
    } catch (GdxRuntimeException e) {
      if (ScienceEngine.DEV_MODE.isDebug()) e.printStackTrace(); 
      return null;
    }
  }

  public static AssetManager getAssetManager() {
    if (assetManager == null) {
      assetManager = new AssetManager();
    }
    return assetManager;
  }

}