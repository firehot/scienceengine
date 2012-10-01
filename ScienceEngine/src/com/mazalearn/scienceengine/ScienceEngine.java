package com.mazalearn.scienceengine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.app.screens.ExperimentMenuScreen;
import com.mazalearn.scienceengine.app.screens.SplashScreen;
import com.mazalearn.scienceengine.app.services.MusicManager;
import com.mazalearn.scienceengine.app.services.PreferencesManager;
import com.mazalearn.scienceengine.app.services.ProfileManager;
import com.mazalearn.scienceengine.app.services.SoundManager;
import com.mazalearn.scienceengine.app.utils.ResourceViewer;

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
  private ResourceViewer resourceViewer;

  private List<String> params;

  private Skin skin;

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
      String kannadachars = 
          "ಕಕಾಕಿಕೀಕುಕೂಕೃಕೆಕೇಕೈಕೊಕೋಕೌಕಂಕಃಕ್ಖಖಾಖಿಖೀಖುಖೂಖೃಖೆಖೇಖೈಖೊಖೋಖೌಖಂಖಃಖ್ಗಗಾಗಿಗೀಗುಗೂಗೃಗೆಗೇಗೈಗೊಗೋಗೌಗಂಗಃಗ್" +
          "ಘಘಾಘಿಘೀಘುಘೂಘೃಘೆಘೇಘೈಘೊಘೋಘೌಘಂಘಃಘ್ಙಙಾಙಿಙೀಙುಙೂಙೃಙೆಙೇಙೈಙೊಙೋಙೌಙಂಙಃಙ್ಚಚಾಚಿಚೀಚುಚೂಚೃಚೆಚೇಚೈಚೊಚೋಚೌಚಂಚಃಚ್" +
          "ಛಛಾಛಿಛೀಛುಛೂಛೃಛೆಛೇಛೈಛೊಛೋಛೌಛಂಛಃಛ್ಜಜಾಜಿಜೀಜುಜೂಜೃಜೆಜೇಜೈಜೊಜೋಜೌಜಂಜಃಜ್ಝಝಾಝಿಝೀಝುಝೂಝೃಝೆಝೇಝೈಝೊಝೋಝೌಝಂಝಃಝ್" +
          "ಞಞಾಞಿಞೀಞುಞೂಞೃಞೆಞೇಞೈಞೊಞೋಞೌಞಂಞಃಞ್ಟಟಾಟಿಟೀಟುಟೂಟೃಟೆಟೇಟೈಟೊಟೋಟೌಟಂಟಃಟ್ಠಠಾಠಿಠೀಠುಠೂಠೃಠೆಠೇಠೈಠೊಠೋಠೌಠಂಠಃಠ್" +
          "ಡಡಾಡಿಡೀಡುಡೂಡೃಡೆಡೇಡೈಡೊಡೋಡೌಡಂಡಃಡ್ಢಢಾಢಿಢೀಢುಢೂಢೃಢೆಢೇಢೈಢೊಢೋಢೌಢಂಢಃಢ್ಣಣಾಣಿಣೀಣುಣೂಣೃಣೆಣೇಣೈಣೊಣೋಣೌಣಂಣಃಣ್" +
          "ತತಾತಿತೀತುತೂತೃತೆತೇತೈತೊತೋತೌತಂತಃತ್ಥಥಾಥಿಥೀಥುಥೂಥೃಥೆಥೇಥೈಥೊಥೋಥೌಥಂಥಃಥ್ದದಾದಿದೀದುದೂದೃದೆದೇದೈದೊದೋದೌದಂದಃದ್" +
          "ಧಧಾಧಿಧೀಧುಧೂಧೃಧೆಧೇಧೈಧೊಧೋಧೌಧಂಧಃಧ್ನನಾನಿನೀನುನೂನೃನೆನೇನೈನೊನೋನೌನಂನಃನ್ಪಪಾಪಿಪೀಪುಪೂಪೃಪೆಪೇಪೈಪೊಪೋಪೌಪಂಪಃಪ್" +
          "ಫಫಾಫಿಫೀಫುಫೂಫೃಫೆಫೇಫೈಫೊಫೋಫೌಫಂಫಃಫ್ಬಬಾಬಿಬೀಬುಬೂಬೃಬೆಬೇಬೈಬೊಬೋಬೌಬಂಬಃಬ್ಭಭಾಭಿಭೀಭುಭೂಭೃಭೆಭೇಭೈಭೊಭೋಭೌಭಂಭಃಭ್" +
          "ಮಮಾಮಿಮೀಮುಮೂಮೃಮೆಮೇಮೈಮೊಮೋಮೌಮಂಮಃಮ್ಯಯಾಯಿಯೀಯುಯೂಯೃಯೆಯೇಯೈಯೊಯೋಯೌಯಂಯಃಯ್ರರಾರಿರೀರುರೂರೃರೆರೇರೈರೊರೋರೌರಂರಃರ್" +
          "ಱಱಾಱಿಱೀಱುಱೂಱೃಱೆಱೇಱೈಱೊಱೋಱೌಱಂಱಃಱ್ಲಲಾಲಿಲೀಲುಲೂಲೃಲೆಲೇಲೈಲೊಲೋಲೌಲಂಲಃಲ್ವವಾವಿವೀವುವೂವೃವೆವೇವೈವೊವೋವೌವಂವಃವ್" +
          "ಶಶಾಶಿಶೀಶುಶೂಶೃಶೆಶೇಶೈಶೊಶೋಶೌಶಂಶಃಶ್ಷಷಾಷಿಷೀಷುಷೂಷೃಷೆಷೇಷೈಷೊಷೋಷೌಷಂಷಃಷ್ಸಸಾಸಿಸೀಸುಸೂಸೃಸೆಸೇಸೈಸೊಸೋಸೌಸಂಸಃಸ್" +
          "ಹಹಾಹಿಹೀಹುಹೂಹೃಹೆಹೇಹೈಹೊಹೋಹೌಹಂಹಃಹ್ಳಳಾಳಿಳೀಳುಳೂಳೃಳೆಳೇಳೈಳೊಳೋಳೌಳಂಳಃಳ್ೞೞಾೞಿೞೀೞುೞೂೞೃೞೆೞೇೞೈೞೊೞೋೞೌೞಂೞಃ";
      kannadachars = "ಪ್ರಯೋಗಗಳು ಮತ್ತೆ";
      String hindichars = 
          "ऀँंःऄअआइईउऊऋऌऍऎएऐऑऒओऔकखगघङचछजझञटठडढणतथदधनऩपफबभमयरऱलळऴवशषसहऺऻ़" +
          "ऽािीुूृॄॅॆेैॉॊोौ्ॎॏॐ॒॑॓॔ॕॖॗक़ख़ग़ज़ड़ढ़फ़य़ॠॡॢॣ।॥०१२३४५६७८९॰ॱॲॳॴॵॶॷॹॺॻॼॽॾॿ";
      StringBuffer pchars = new StringBuffer();
      for (int i = 0; i < kannadachars.length(); i++) {
        if (kannadachars.indexOf(kannadachars.charAt(i)) == i) {
          pchars.append(kannadachars.charAt(i));
        }
      }
      FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("skin/iitmkann.ttf"));
      BitmapFont kannadaFont = generator.generateFont(15, pchars.toString(), false);
      generator.dispose();
      skin.add("kannada", kannadaFont);
    }
    return skin;
  }
  
  // ResourceViewer interface
  
  public void setUrlViewer(ResourceViewer resourceViewer) {
    this.resourceViewer = resourceViewer;
  }
  
  public void browseURL(String url){
    if (resourceViewer != null) {
      resourceViewer.browseURL(url);
    }
  }


  public boolean playVideo(File file) {
    if (resourceViewer != null) {
      return resourceViewer.playVideo(file);
    }
    return false;
  }
  // Game-related methods

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

    // show the splash screen when the scienceEngine is resized for the first time;
    // this approach avoids calling the screen's resize method repeatedly
    if (getScreen() == null) {
      setScreen(DEV_MODE != DevMode.PRODUCTION ? new ExperimentMenuScreen(this) : new SplashScreen(this));
    }
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