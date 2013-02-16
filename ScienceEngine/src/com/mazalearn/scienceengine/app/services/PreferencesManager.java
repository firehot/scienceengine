package com.mazalearn.scienceengine.app.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;

/**
 * Handles the scienceEngine preferences.
 * Preferences are global across users.
 * Profile is per user - stored as a preference against email address of profile
 */
public class PreferencesManager {
  // constants
  private static final String PREF_VOLUME = "volume";
  private static final String PREF_MUSIC_ENABLED = "music.enabled";
  private static final String PREF_SOUND_ENABLED = "sound.enabled";
  // Active user
  private static final String PREF_USER_EMAIL = "user.email";
  private static final String PREFS_NAME = "scienceengine";
  private static final String PREF_PROFILE = "profile";
  private Profile profile;

  public PreferencesManager() {
    retrieveProfile();
  }

  protected Preferences getPrefs() {
    return Gdx.app.getPreferences(PREFS_NAME);
  }

  public boolean isSoundEnabled() {
    return getPrefs().getBoolean(PREF_SOUND_ENABLED, true);
  }

  public void setSoundEnabled(boolean soundEffectsEnabled) {
    getPrefs().putBoolean(PREF_SOUND_ENABLED, soundEffectsEnabled);
    getPrefs().flush();
  }

  public boolean isMusicEnabled() {
    return getPrefs().getBoolean(PREF_MUSIC_ENABLED, false);
  }

  public void setMusicEnabled(boolean musicEnabled) {
    getPrefs().putBoolean(PREF_MUSIC_ENABLED, musicEnabled);
    getPrefs().flush();
  }

  public float getVolume() {
    return getPrefs().getFloat(PREF_VOLUME, 0.5f);
  }

  public void setVolume(float volume) {
    getPrefs().putFloat(PREF_VOLUME, volume);
    getPrefs().flush();
  }
  
  public Profile loadProfile(String userEmail) {
    getPrefs().putString(PREF_USER_EMAIL, userEmail);
    getPrefs().flush();
    return retrieveProfile();
  }

  private Profile retrieveProfile() {
    String userEmail = getPrefs().getString(PREF_USER_EMAIL);
    String profileAsText = getPrefs().getString(userEmail);
    if (profileAsText != null && !profileAsText.isEmpty()) {
      // decode the contents - base64 encoded
      profileAsText = Base64Coder.decodeString(profileAsText);
      profile = new Json().fromJson(Profile.class, profileAsText);
    } else {
      profile = new Profile();
      profile.setUserEmail(userEmail);
      saveProfile();
    }
    return profile;
  }

  public Profile getProfile() {
    if (profile != null) {
      return profile;
    }
    return retrieveProfile();
  }

  public void saveProfile() {
    // convert the given profile to text
    String profileAsText = new Json().toJson(profile);
    profileAsText = Base64Coder.encodeString(profileAsText);
    String userEmail = getPrefs().getString(PREF_USER_EMAIL);
    getPrefs().putString(userEmail, profileAsText);
    getPrefs().flush();
  }
}
