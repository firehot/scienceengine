package com.mazalearn.scienceengine;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mazalearn.scienceengine.molecule.LJMolecularModel;
import com.mazalearn.scienceengine.molecule.MolecularModel;
import com.mazalearn.scienceengine.screens.SplashScreen;
import com.mazalearn.scienceengine.screens.StartScreen;
import com.mazalearn.scienceengine.services.MusicManager;
import com.mazalearn.scienceengine.services.PreferencesManager;
import com.mazalearn.scienceengine.services.ProfileManager;
import com.mazalearn.scienceengine.services.SoundManager;

public class ScienceEngine extends Game {
  // constant useful for logging
  public static final String LOG = ScienceEngine.class.getName();

  // whether we are in development mode
  public static final boolean DEV_MODE = true;

  // a libgdx helper class that logs the current FPS each second
  private FPSLogger fpsLogger;

  // services
  private PreferencesManager preferencesManager;
  private ProfileManager profileManager;
  private MusicManager musicManager;
  private SoundManager soundManager;

  // Services' getters

  public PreferencesManager getPreferencesManager() {
    return preferencesManager;
  }

  public ProfileManager getProfileManager() {
    return profileManager;
  }

  public MusicManager getMusicManager() {
    return musicManager;
  }

  public SoundManager getSoundManager() {
    return soundManager;
  }

  // Game-related methods

  @Override
  public void create() {
    Gdx.app.log(ScienceEngine.LOG, "Creating game on " + Gdx.app.getType());

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

    // create the helper objects
    fpsLogger = new FPSLogger();
  }

  @Override
  public void resize(int width, int height) {
    super.resize(width, height);
    Gdx.app.log(ScienceEngine.LOG, "Resizing game to: " + width + " x "
        + height);

    // show the splash screen when the game is resized for the first time;
    // this approach avoids calling the screen's resize method repeatedly
    if (getScreen() == null) {
      setScreen(DEV_MODE ? new StartScreen(this) : new SplashScreen(this));
    }
  }

  @Override
  public void render() {
    super.render();
    // output the current FPS
    if (DEV_MODE) {
      fpsLogger.log();
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
    // back to the game
    profileManager.persist();
  }

  @Override
  public void resume() {
    super.resume();
    Gdx.app.log(ScienceEngine.LOG, "Resuming game");
  }

  @Override
  public void setScreen(Screen screen) {
    super.setScreen(screen);
    Gdx.app.log(ScienceEngine.LOG, "Setting screen: "
        + screen.getClass().getName());
  }

}