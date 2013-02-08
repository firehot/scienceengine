package com.mazalearn.scienceengine;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.app.screens.ActivityScreen;
import com.mazalearn.scienceengine.app.screens.DomainHomeScreen;
import com.mazalearn.scienceengine.app.screens.LoadingScreen;
import com.mazalearn.scienceengine.app.screens.SplashScreen;
import com.mazalearn.scienceengine.app.services.EventLog;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.app.services.MusicManager;
import com.mazalearn.scienceengine.app.services.PreferencesManager;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.ProfileManager;
import com.mazalearn.scienceengine.app.services.SoundManager;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.services.loaders.AsyncLevelLoader;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter.Platform;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.IParameter;
import com.mazalearn.scienceengine.core.model.Parameter;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.IScience2DView;

public class ScienceEngine extends Game {
  // constant useful for logging
  public static final String LOG = ScienceEngine.class.getName();

  // mode of development
  public enum DevMode {PRODUCTION, DEBUG, DESIGN};
  public static DevMode DEV_MODE = DevMode.PRODUCTION;
  
  // Provide access to this singleton scienceEngine from any class
  public static ScienceEngine SCIENCE_ENGINE;

  // a libgdx helper class that logs the current FPS each second
  private FPSLogger fpsLogger;

  // base of skin being used
  private static String SKIN_BASE = "skin/uiskin1";
  
  // services
  private static PreferencesManager preferencesManager;
  private static ProfileManager profileManager;
  private static MusicManager musicManager;
  private static SoundManager soundManager;
  private static IPlatformAdapter platformAdapter;
  public static AssetManager assetManager;
  private static Skin skin;

  private String uri;

  private TextureAtlas atlas;


  private static Science2DBody selectedBody;

  private static boolean isProbeMode;

  private static EventLog eventLog = new EventLog();

  private static float time;

  private static Set<Science2DBody> pinnedBodies = new HashSet<Science2DBody>();

  private static long logicalTime;

  public ScienceEngine(String url) {
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
      FileHandle skinFile = Gdx.files.internal(SKIN_BASE + ".json");
      skin = new Skin(skinFile, new TextureAtlas(Gdx.files.internal(SKIN_BASE + ".atlas")));
      skin.add("en", skin.getFont(ScreenComponent.getFont()));
      getMsg().setFont(skin);
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

  // IPlatformAdapter interface
  
  public void setPlatformAdapter(IPlatformAdapter platformAdapter) {
    ScienceEngine.platformAdapter = platformAdapter;
  }
  
  public void browseURL(String url){
    getPlatformAdapter().browseURL(url);
  }


  public boolean playVideo(File file) {
    return getPlatformAdapter().playVideo(file);
  }
  // Game-related methods
  
  @Override
  public void create() {
    Gdx.app.log(ScienceEngine.LOG, "Creating Engine on " + Gdx.app.getType());
    Gdx.app.log(ScienceEngine.LOG, "With params " + uri);
    // Resize to full screen
    //Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode());

    // create the preferences Manager
    preferencesManager = new PreferencesManager();

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

    // create the profile Manager
    profileManager = new ProfileManager();
    profileManager.retrieveProfile();
    
    // create the asset Manager
    assetManager = new AssetManager();
    assetManager.setLoader(IScience2DController.class, 
        new AsyncLevelLoader(new InternalFileHandleResolver()));

    //if (DEV_MODE != DevMode.PRODUCTION) {
      // create the helper objects
      fpsLogger = new FPSLogger();
    //}
    
    DisplayMode displayMode = Gdx.graphics.getDesktopDisplayMode();
    ScreenComponent.setSize(1024, 768); // displayMode.width, displayMode.height);
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
      setScreen(new LoadingScreen(this, createScreen(uri)));
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
        query = uri.substring(qmark + 1);
      }
      String[] pathSegments = path.toLowerCase().split("/");
      int i = 3;
      if (pathSegments.length > i && isSupportedSubject(pathSegments[i])) i++;
      if (pathSegments.length > i && isSupportedBoard(pathSegments[i])) i++;
      if (pathSegments.length > i && findSupportedDomain(pathSegments[i]) != null) {
        String domain = findSupportedDomain(pathSegments[i]);
        Integer iLevel = null;
        if (query != null) {
          String[] queryParts = query.toLowerCase().split("&");
          if (queryParts[0].startsWith("level")) {
            iLevel = Integer.parseInt(queryParts[0].substring("level=".length()));
          }
        }
        if (iLevel == null) {
          return new DomainHomeScreen(this, domain);
        }
        
        return new ActivityScreen(this, domain, iLevel);
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
  
  private String findSupportedDomain(String token) {
    String[] topics = new String[] {"Electromagnetism", "Waves", "StatesOfMatter"};
    for (String topic: topics) {
      if (topic.toLowerCase().equals(token)) return topic;
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

  public static IPlatformAdapter getPlatformAdapter() {
	if (platformAdapter == null) {
		platformAdapter = new AbstractPlatformAdapter(Platform.IOS);
	}
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
    getSoundManager().play(ScienceEngineSound.CLICK);
    selectedBody = body;
    if (body == null) return;
    eventLog.logEvent(body.name(), Parameter.Select.name());
    science2DView.checkGuruProgress();
    displayEntityStatus(body, body.getComponentTypeName(), science2DView);
  }

  /**
   * Parameter got selected by user. Record this event and display status
   * @param parameter
   * @param science2DView
   */
  public static void selectParameter(Science2DBody body, IParameter parameter, float value, IScience2DView science2DView) {
    getSoundManager().play(ScienceEngineSound.CLICK);
    displayEntityStatus(body, parameter.name(), science2DView);
    if (body == null) return;
    science2DView.checkGuruProgress();
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
    String component = body != null ? body.toString() + " - " : "";
    if (entityName.contains(".")) {
      entityName = entityName.substring(0, entityName.indexOf("."));
    }
    String message = component + getMsg().getString("Help." + entityName);
    displayStatusMessage(stage, message);
  }

  public static void displayStatusMessage(IScience2DView stage, String message) {
    Label status = (Label) stage.findActor(ScreenComponent.Status.name());
    if (status != null) // TODO: only for level editor - why?
    status.setText(message);
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

  public static String getUserEmail() {
    Profile profile = profileManager.retrieveProfile();
    return profile.getUserEmail();
  }

  public static String getUserName() {
    Profile profile = profileManager.retrieveProfile();
    return profile.getUserName();
  }

  public static String getHostPort() {
    if (ScienceEngine.DEV_MODE == DevMode.PRODUCTION) {
      return "www.mazalearn.com:80";
    }
    return "localhost:8888";
  }

  public static long getLogicalTime() {
    return logicalTime;
  }
}