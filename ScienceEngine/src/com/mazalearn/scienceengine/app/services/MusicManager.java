package com.mazalearn.scienceengine.app.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.mazalearn.scienceengine.ScienceEngine;

/**
 * A service that manages the background music.
 * <p>
 * Only one music may be playing at a given timeLimit.
 */
public class MusicManager implements Disposable {
  /**
   * The available music files.
   */
  public enum ScienceEngineMusic {
    MENU("music/vivaldi-spring-1.mp3"), 
    LEVEL("music/vivaldi-spring-2.mp3"), 
    HUM("music/car-humming.mp3");

    private String fileName;
    private Music musicResource;

    private ScienceEngineMusic(String fileName) {
      this.fileName = fileName;
    }

    public String getFileName() {
      return fileName;
    }

    public Music getMusicResource() {
      return musicResource;
    }

    public void setMusicResource(Music musicBeingPlayed) {
      this.musicResource = musicBeingPlayed;
    }
  }

  private static final float ATTENUATION = 0.5f;

  /**
   * Holds the music currently being played, if any.
   */
  private ScienceEngineMusic musicBeingPlayed;

  /**
   * The volume to be set on the music.
   */
  private float volume = ATTENUATION * 1f;

  /**
   * Whether the music is enabled.
   */
  private boolean enabled = true;

  /**
   * Creates the music assetManager.
   */
  public MusicManager() {
  }

  /**
   * Plays the given music (starts the streaming).
   * <p>
   * If there is already a music being played it is stopped automatically.
   */
  public void play(ScienceEngineMusic music) {
    // check if the music is enabled
    if (!enabled)
      return;

    // check if the given music is already being played
    if (musicBeingPlayed == music)
      return;

    // do some logging
    Gdx.app.log(ScienceEngine.LOG, "Playing music: " + music.name());

    // stop any music being played
    stop();

    // start streaming the new music
    FileHandle musicFile = Gdx.files.internal(music.getFileName());
    Music musicResource = Gdx.audio.newMusic(musicFile);
    // Lower volume on music
    musicResource.setLooping(true);
    musicResource.play();

    // set the music being played
    musicBeingPlayed = music;
    musicBeingPlayed.setMusicResource(musicResource);
  }

  /**
   * Stops and disposes the current music being played, if any.
   */
  public void stop() {
    if (musicBeingPlayed != null) {
      Gdx.app.log(ScienceEngine.LOG, "Stopping current music");
      Music musicResource = musicBeingPlayed.getMusicResource();
      musicResource.stop();
      musicResource.dispose();
      musicBeingPlayed = null;
    }
  }

  /**
   * Sets the music volume which must be inside the range [0,1].
   */
  public void setVolume(float volume) {
    Gdx.app.log(ScienceEngine.LOG, "Adjusting music volume to: " + volume);

    // check and set the new volume
    if (volume < 0 || volume > 1f) {
      throw new IllegalArgumentException(
          "The volume must be inside the range: [0,1]");
    }
    this.volume = ATTENUATION * volume;

    // if there is a music being played, change its volume
    if (musicBeingPlayed != null) {
      musicBeingPlayed.getMusicResource().setVolume(ATTENUATION * volume);
    }
  }
  
  /**
   * @return current volume
   */
  public float getVolume() {
    return volume;
  }

  /**
   * Enables or disabled the music.
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;

    // if the music is being deactivated, stop any music being played
    if (!enabled) {
      stop();
    }
  }

  /**
   * Disposes the music assetManager.
   */
  public void dispose() {
    Gdx.app.log(ScienceEngine.LOG, "Disposing music assetManager");
    stop();
  }
}
