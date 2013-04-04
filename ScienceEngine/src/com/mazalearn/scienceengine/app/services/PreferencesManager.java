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
 * Profile is per user - stored as a preference against email address of userProfile
 */
public class PreferencesManager {
  private static final String SERVER_PROFILE_PREFIX = "SERVER_";
  private static final String SYNC_PROFILES = "syncprofiles";
  private static final String INSTALL_PROFILE = "installprofile";
  // constants
  private static final String PREF_VOLUME = "volume";
  private static final String PREF_MUSIC_ENABLED = "music.enabled";
  private static final String PREF_SOUND_ENABLED = "sound.enabled";
  // Active user
  private static final String PREFS_NAME = "scienceengine";
  private Profile userProfile;
  private Preferences prefs;
  private InstallProfile installProfile;

  public PreferencesManager() {
    prefs = Gdx.app.getPreferences(PREFS_NAME);
    retrieveUserProfile();
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
  
  // precondition: userprofile has an email
  public void setActiveUserProfile(Profile userProfile) {
    prefs.putString(Profile.USER_ID, userProfile.getUserEmail());
    prefs.flush();
    this.userProfile = userProfile;
  }

  private Profile retrieveUserProfile() {
    String userId = getProfileUserId();
    userProfile = getUserProfile(userId);
    userProfile.setPlatform(ScienceEngine.getPlatformAdapter().getPlatform());
    if (userProfile.getUserEmail().length() > 0) {
      prefs.putString(Profile.USER_ID, userProfile.getUserEmail());
      prefs.flush();
    }
    saveUserProfile();

    return userProfile;
  }

  public Profile getUserProfile(String userId) {
    String profileBase64 = prefs.getString(userId);
    Profile profile = Profile.fromBase64(profileBase64);
    if (profile == null) {
      profile = new Profile();
    }
    return profile;
  }

  private String getProfileUserId() {
    String userId = prefs.getString(Profile.USER_ID);
    if (userId == null || userId.length() == 0) {
      userId = ScienceEngine.getPlatformAdapter().getInstallationId();
    }
    return userId;
  }

  public Profile getActiveUserProfile() {
    if (userProfile != null) {
      return userProfile;
    }
    return retrieveUserProfile();
  }

  public InstallProfile getInstallProfile() {
    if (installProfile == null) {
      installProfile = InstallProfile.fromBase64((String) prefs.getString(INSTALL_PROFILE));
      if (installProfile == null) {
        installProfile = new InstallProfile();
        installProfile.save();
      }
    }
    return installProfile;
  }

  private String getSyncProfilesString() {
    String s = prefs.getString(SYNC_PROFILES);
    return s == null ? "" : s;
  }
  
  private void setSyncProfilesString(String s) {
    prefs.putString(SYNC_PROFILES, s);
    prefs.flush();
    Gdx.app.log(ScienceEngine.LOG, "Set sync userProfile: " + prefs.getString(SYNC_PROFILES));
  }
  
  public void saveUserProfile() {
    saveProfile(userProfile);
  }

  private void saveProfile(Profile profile) {
    // convert the given userProfile to text
    String localProfileBase64 = profile.toBase64();
    String userId = getProfileUserId();
    String serverProfileBase64 = prefs.getString(SERVER_PROFILE_PREFIX + userId);
    if (serverProfileBase64 != null) {
      profile.mergeProfile(serverProfileBase64);
      prefs.remove(SERVER_PROFILE_PREFIX + userId);
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
          // Post userProfile to server and get back updated server userProfile
          String serverProfileBase64 =
              ScienceEngine.getPlatformAdapter().httpPost("/profile", "application/octet-stream", 
                  postParams, localProfileBase64.getBytes());
          prefs.putString(SERVER_PROFILE_PREFIX + userId, serverProfileBase64);
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
    ScienceEngine.getPlatformAdapter().executeAsync(new SyncProfiles(syncProfilesString, prefs));
    setSyncProfilesString("");
  }

  public void saveInstallProfile() {
    prefs.putString(INSTALL_PROFILE, installProfile.toBase64());
    prefs.flush();
  }
}
