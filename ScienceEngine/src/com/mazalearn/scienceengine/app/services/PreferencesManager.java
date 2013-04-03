package com.mazalearn.scienceengine.app.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;

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
  private Preferences prefs;

  public PreferencesManager() {
    prefs = Gdx.app.getPreferences(PREFS_NAME);
    retrieveProfile();
  }

  public boolean isSoundEnabled() {
    return prefs.getBoolean(PREF_SOUND_ENABLED, true);
  }

  public void setSoundEnabled(boolean soundEffectsEnabled) {
    prefs.putBoolean(PREF_SOUND_ENABLED, soundEffectsEnabled);
  }

  public boolean isMusicEnabled() {
    return prefs.getBoolean(PREF_MUSIC_ENABLED, false);
  }

  public void setMusicEnabled(boolean musicEnabled) {
	  prefs.flush();
    prefs.putBoolean(PREF_MUSIC_ENABLED, musicEnabled);
    prefs.flush();
  }

  public float getVolume() {
    return prefs.getFloat(PREF_VOLUME, 0.5f);
  }

  public void setVolume(float volume) {
    prefs.putFloat(PREF_VOLUME, volume);
    prefs.flush();
  }
  
  public Profile loadProfile(String userId) {
    prefs.putString(Profile.USER_ID, userId.toLowerCase());
    prefs.flush();
    return retrieveProfile();
  }

  private Profile retrieveProfile() {
    String userId = getProfileUserId();
    // Retrieve from local file system
    String localProfileBase64 = prefs.getString(userId);
    // Retrieve from server
    String serverProfileBase64 = ScienceEngine.getPlatformAdapter().httpGet(
        "/profile?" + Profile.USER_ID + "=" + userId);
    profile = Profile.mergeProfiles(localProfileBase64, serverProfileBase64); 
    profile.setPlatform(ScienceEngine.getPlatformAdapter().getPlatform());
    if (profile.getUserEmail().length() > 0) {
      prefs.putString(Profile.USER_ID, profile.getUserEmail());
      prefs.flush();
    }
    saveProfile();

    return profile;
  }

  private String getProfileUserId() {
    String userId = prefs.getString(Profile.USER_ID);
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
    String s = prefs.getString(SYNC_PROFILES);
    return s == null ? "" : s;
  }
  
  private void setSyncProfilesString(String s) {
    prefs.putString(SYNC_PROFILES, s);
    prefs.flush();
    Gdx.app.log(ScienceEngine.LOG, "Set sync profile: " + prefs.getString(SYNC_PROFILES));
  }
  
  public void saveProfile() {
    saveProfile(profile);
  }

  private void saveProfile(Profile profile) {
    // convert the given profile to text
    String localProfileBase64 = profile.toBase64();
    String userId = getProfileUserId();
    String serverProfileBase64 = prefs.getString("SERVER_" + userId);
    if (serverProfileBase64 != null) {
      profile.mergeProfile(serverProfileBase64);
      prefs.remove("SERVER_" + userId);
    }

    String savedProfile = prefs.getString(userId);
    // No need to save if already up to date
    if (localProfileBase64.equals(savedProfile)) return;
    
    prefs.putString(userId, localProfileBase64);
    markProfileDirty(userId);
    Gdx.app.log(ScienceEngine.LOG, "Saved Profile - " + userId);
  }

  private void markProfileDirty(String userId) {
    // Add to set of profiles which need to be synced to server
    String syncProfilesString = getSyncProfilesString();
    if (!syncProfilesString.startsWith(userId + "\n")) {
      setSyncProfilesString(userId + "\n" + syncProfilesString);
    }
    prefs.flush();
  }
  
  private static class SyncProfiles implements Runnable {

    private String syncProfilesString;
    private Preferences prefs;
    
    public SyncProfiles(String syncProfilesString, Preferences prefs) {
      this.prefs = prefs;
      this.syncProfilesString = syncProfilesString;
    }
    @Override
    public void run() {
      Map<String, String> postParams = new HashMap<String, String>();
      String[] syncProfilesArray = syncProfilesString.split("\n");
      HashSet<String> syncProfiles = new HashSet<String>(Arrays.asList(syncProfilesArray));
      for (String userId: syncProfiles) {
        String localProfileBase64 = prefs.getString(userId);
        postParams.put(Profile.USER_ID, userId);
        try {
          // Post profile to server and get back updated server profile
          String serverProfileBase64 =
              ScienceEngine.getPlatformAdapter().httpPost("/profile", "application/octet-stream", 
                  postParams, localProfileBase64.getBytes());
          prefs.putString("SERVER_" + userId, serverProfileBase64);
          Gdx.app.log(ScienceEngine.LOG, "Sync Profile to MazaLearn - " + userId);
        } catch(GdxRuntimeException e) {
          if (ScienceEngine.DEV_MODE == DevMode.DEBUG) e.printStackTrace();
          Gdx.app.log(ScienceEngine.LOG, "Network Problem: Failed to sync - " + userId);
        }
      }
    }
    
  }

  public void syncProfiles() {
    Gdx.app.log(ScienceEngine.LOG, "Syncing Profiles");
    String syncProfilesString = prefs.getString(SYNC_PROFILES);
    Gdx.app.log(ScienceEngine.LOG, "Sync Profile: " + syncProfilesString);
    if (syncProfilesString.length() == 0) return;
    Thread syncThread = new Thread(new SyncProfiles(syncProfilesString, prefs), "syncthread");
    syncThread.start();
    setSyncProfilesString("");
  }
}
