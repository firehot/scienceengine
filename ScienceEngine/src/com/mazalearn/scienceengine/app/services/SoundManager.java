package com.mazalearn.scienceengine.app.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.LRUCache;
import com.mazalearn.scienceengine.app.utils.LRUCache.CacheEntryRemovedListener;

/**
 * A service that manages the sound effects.
 */
public class SoundManager implements
    CacheEntryRemovedListener<ScienceEngineSound, Sound>, Disposable {
  /**
   * The available sound files.
   */
  public enum ScienceEngineSound {
    CLICK("sound/click.mp3"), 
    SUCCESS("sound/success.mp3"), 
    FAILURE("sound/failure.mp3"),
    CELEBRATE("sound/celebrate.mp3");

    private final String fileName;

    private ScienceEngineSound(String fileName) {
      this.fileName = fileName;
    }

    public String getFileName() {
      return fileName;
    }
  }

  /**
   * The volume to be set on the sound.
   */
  private float volume = 1f;

  /**
   * Whether the sound is enabled.
   */
  private boolean enabled = true;

  /**
   * The sound cache.
   */
  private final LRUCache<ScienceEngineSound, Sound> soundCache;

  /**
   * Creates the sound assetManager.
   */
  public SoundManager() {
    soundCache = new LRUCache<SoundManager.ScienceEngineSound, Sound>(10);
    soundCache.setEntryRemovedListener(this);
  }

  /**
   * Plays the specified sound.
   */
  public void play(ScienceEngineSound sound) {
    // check if the sound is enabled
    if (!enabled)
      return;

    // try and get the sound from the cache
    Sound soundToPlay = soundCache.get(sound);
    if (soundToPlay == null) {
      FileHandle soundFile = Gdx.files.internal(sound.getFileName());
      soundToPlay = Gdx.audio.newSound(soundFile);
      soundCache.add(sound, soundToPlay);
    }

    // play the sound
    Gdx.app.log(ScienceEngine.LOG, "Playing sound: " + sound.name());
    soundToPlay.play(volume);
  }

  /**
   * Sets the sound volume which must be inside the range [0,1].
   */
  public void setVolume(float volume) {
    Gdx.app.log(ScienceEngine.LOG, "Adjusting sound volume to: " + volume);

    // check and set the new volume
    if (volume < 0 || volume > 1f) {
      throw new IllegalArgumentException(
          "The volume must be inside the range: [0,1]");
    }
    this.volume = volume;
  }

  /**
   * Enables or disabled the sound.
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  // EntryRemovedListener implementation

  @Override
  public void notifyEntryRemoved(ScienceEngineSound key, Sound value) {
    Gdx.app.log(ScienceEngine.LOG, "Disposing sound: " + key.name());
    value.dispose();
  }

  /**
   * Disposes the sound assetManager.
   */
  public void dispose() {
    Gdx.app.log(ScienceEngine.LOG, "Disposing sound assetManager");
    for (Sound sound : soundCache.retrieveAll()) {
      sound.stop();
      sound.dispose();
    }
  }
}
