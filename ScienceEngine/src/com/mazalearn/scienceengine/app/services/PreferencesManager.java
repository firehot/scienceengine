package com.mazalearn.scienceengine.app.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mazalearn.scienceengine.ScienceEngine;

/**
 * Handles the scienceEngine preferences.
 * Preferences are global across users.
 * Profile is per user - stored as a preference against email address of profile
 */
public class PreferencesManager {
  private static final String SYNC_PROFILES = "syncprofiles";
  // constants
  private static final String PREF_VOLUME = "volume";
  private static final String PREF_MUSIC_ENABLED = "music.enabled";
  private static final String PREF_SOUND_ENABLED = "sound.enabled";
  // Active user
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
  
  public Profile loadProfile(String userId) {
    getPrefs().putString(Profile.USER_ID, userId.toLowerCase());
    getPrefs().flush();
    return retrieveProfile();
  }

  private Profile retrieveProfile() {
    String userId = getProfileUserId();
    // Retrieve from local file system
    String localProfileBase64 = getPrefs().getString(userId);
    // Retrieve from server
    String serverProfileBase64 = ScienceEngine.getPlatformAdapter().httpGet(
        "/profile?" + Profile.USER_ID + "=" + userId);
    profile = Profile.mergeProfiles(localProfileBase64, serverProfileBase64); 
    profile.setPlatform(ScienceEngine.getPlatformAdapter().getPlatform());
    if (profile.getUserEmail().length() > 0) {
      getPrefs().putString(Profile.USER_ID, profile.getUserEmail());
      getPrefs().flush();
    }
    saveProfile();

    return profile;
  }

  private String getProfileUserId() {
    String userId = getPrefs().getString(Profile.USER_ID);
    if (userId == null || userId.length() == 0) {
      userId = ScienceEngine.getPlatformAdapter().getInstallationId();
    }
    return userId;
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
    saveProfile(profile);
  }

  private void saveProfile(Profile profile) {
    // convert the given profile to text
    String profileAsBase64 = profile.getBase64();
    String userId = getProfileUserId();
    String savedProfile = getPrefs().getString(userId);
    // No need to save if already up to date
    if (profileAsBase64.equals(savedProfile)) return;
    
    getPrefs().putString(userId, profileAsBase64);
    // Add to set of profiles which need to be synced to server
    String syncProfilesString = getSyncProfilesString();
    if (!syncProfilesString.startsWith(userId + "\n")) {
      setSyncProfilesString(userId + "\n" + syncProfilesString);
    }
    getPrefs().flush();
    Gdx.app.log(ScienceEngine.LOG, "Saved Profile - " + userId);
  }

  public void syncProfiles() {
    Gdx.app.log(ScienceEngine.LOG, "Syncing Profiles");
    Map<String, String> postParams = new HashMap<String, String>();
    String syncProfilesString = getPrefs().getString(SYNC_PROFILES);
    if (syncProfilesString.length() == 0) return;
    
    String[] syncProfilesArray = syncProfilesString.split("\n");
    HashSet<String> syncProfiles = new HashSet<String>(Arrays.asList(syncProfilesArray));
    for (String userId: syncProfiles) {
      String localProfileBase64 = getPrefs().getString(userId);
      postParams.put(Profile.USER_ID, userId);
      try {
        // Post profile to server and get back updated server profile
        String serverProfileBase64 =
            ScienceEngine.getPlatformAdapter().httpPost("/profile", "application/octet-stream", 
                postParams, localProfileBase64.getBytes());
        Profile profile = Profile.mergeProfiles(localProfileBase64, serverProfileBase64);
        saveProfile(profile);
        // push server updates (if any) into current active profile
        if (userId.equals(getProfileUserId())) {
          this.profile = profile;
          getPrefs().putString(Profile.USER_ID, profile.getUserEmail());
        }
        Gdx.app.log(ScienceEngine.LOG, "Uploaded Profile to MazaLearn - " + userId);
      } catch(GdxRuntimeException e) {
        e.printStackTrace();
        Gdx.app.log(ScienceEngine.LOG, "Network Problem: Failed to upload - " + userId);
      }
    }
    setSyncProfilesString("");
  }
}
