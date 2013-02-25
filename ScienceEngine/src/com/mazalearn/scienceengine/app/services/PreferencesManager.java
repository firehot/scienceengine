package com.mazalearn.scienceengine.app.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.mazalearn.scienceengine.ScienceEngine;

/**
 * Handles the scienceEngine preferences.
 * Preferences are global across users.
 * Profile is per user - stored as a preference against email address of profile
 */
public class PreferencesManager {
  private static final String USER_EMAIL = "useremail";
  private static final String SYNC_PROFILES = "syncprofiles";
  // constants
  private static final String PREF_VOLUME = "volume";
  private static final String PREF_MUSIC_ENABLED = "music.enabled";
  private static final String PREF_SOUND_ENABLED = "sound.enabled";
  // Active user
  private static final String PREF_USER_EMAIL = "useremail";
  private static final String PREFS_NAME = "scienceengine";
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
    if (userEmail == null || userEmail.length() == 0) {
      userEmail = "DemoUser@mazalearn.com";
    }
    String profileAsText = getPrefs().getString(userEmail);
    if (profileAsText != null && profileAsText.length() > 0) {
      // decode the contents - base64 encoded
      profileAsText = Base64Coder.decodeString(profileAsText);
      profile = new Json().fromJson(Profile.class, profileAsText);
    } else { // Create a new Profile
      profile = new Profile();
      profile.setUserEmail(userEmail);
      profile.setUserName(userEmail.substring(0, userEmail.indexOf("@")));
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

  private String getSyncProfilesString() {
    String s = getPrefs().getString(SYNC_PROFILES);
    return s == null ? "" : s;
  }
  
  private void setSyncProfilesString(String s) {
    getPrefs().putString(SYNC_PROFILES, s);
    getPrefs().flush();
  }
  
  public void saveProfile() {
    // convert the given profile to text
    String profileAsText = new Json().toJson(profile);
    profileAsText = Base64Coder.encodeString(profileAsText);
    String userEmail = getPrefs().getString(PREF_USER_EMAIL);
    String savedProfile = getPrefs().getString(userEmail);
    // No need to save if already up to date
    if (profileAsText.equals(savedProfile)) return;
    
    getPrefs().putString(userEmail, profileAsText);
    // Add to set of profiles which need to be synced to server
    String syncProfilesString = getSyncProfilesString();
    if (!syncProfilesString.startsWith(userEmail + "\n")) {
      setSyncProfilesString(userEmail + "\n" + syncProfilesString);
    }
    getPrefs().flush();
    Gdx.app.log(ScienceEngine.LOG, "Saved Profile - " + userEmail);
  }
  
  public void syncProfiles() {
    Map<String, String> postParams = new HashMap<String, String>();
    String syncProfilesString = getPrefs().getString(SYNC_PROFILES);
    if (syncProfilesString.length() == 0) return;
    
    String[] syncProfilesArray = syncProfilesString.split("\n");
    HashSet<String> syncProfiles = new HashSet<String>(Arrays.asList(syncProfilesArray));
    for (String profileKey: syncProfiles) {
      String encodedProfile = getPrefs().getString(profileKey);
      postParams.put(USER_EMAIL, profileKey);
      try {
        // Post profile to server
        ScienceEngine.getPlatformAdapter().httpPost("/profile", "application/octet-stream", 
            postParams, encodedProfile.getBytes());
        Gdx.app.log(ScienceEngine.LOG, "Uploaded Profile to MazaLearn - " + profileKey);
      } catch(GdxRuntimeException e) {
        e.printStackTrace();
        Gdx.app.log(ScienceEngine.LOG, "Network Problem: Failed to upload - " + profileKey);
      }
    }
    setSyncProfilesString("");
  }
}
