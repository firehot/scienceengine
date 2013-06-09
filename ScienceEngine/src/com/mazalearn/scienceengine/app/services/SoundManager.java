package com.mazalearn.scienceengine.app.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.mazalearn.scienceengine.ScienceEngine;

/**
 * A service that manages the sound effects.
 */
public class SoundManager {
  /**
   * The available sound files.
   */
  public enum ScienceEngineSound {
    CLICK("sound/click.mp3"), 
    SUCCESS("sound/success.mp3"), 
    FAILURE("sound/failure.mp3"),
    CELEBRATE("sound/celebrate.mp3"),
    RAPID_FIRE("sound/rapidfire.mp3"),
    CHIME("sound/chime.mp3"),
    CHALLENGE("sound/challenge.mp3");

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
   * Creates the sound assetManager.
   */
  public SoundManager() {
  }

  /**
   * Plays the specified sound.
   */
  public void play(ScienceEngineSound sound) {
    // check if the sound is enabled
    if (!enabled)
      return;

    // try and get the sound from the cache
    Sound soundToPlay = ScienceEngine.getAssetManager().get(sound.getFileName(), Sound.class);
    if (soundToPlay == null) {
      FileHandle soundFile = Gdx.files.internal(sound.getFileName());
      soundToPlay = Gdx.audio.newSound(soundFile);
    }

    // play the sound
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
   * Enables or disables the sound.
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Disposes the sound assetManager.
   */
  public void dispose() {
    Gdx.app.log(ScienceEngine.LOG, "Disposing sound assetManager");
  }
}
