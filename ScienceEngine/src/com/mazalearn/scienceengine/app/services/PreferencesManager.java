package com.mazalearn.scienceengine.app.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;
import com.mazalearn.scienceengine.app.utils.ServerConstants;

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
  private static final String PREF_SPEECH_ENABLED = "speech.enabled";
  private static final String PREF_SOUND_ENABLED = "sound.enabled";
  private static final String PREFS_NAME = "scienceengine";
  private static final String PREFS_SYNC_MODE = "syncmode";
  private final Preferences prefs;
  private final InstallProfile defaultInstallProfile;
  private final IPlatformAdapter platformAdapter;
  private InstallProfile installProfile;
  // Active user
  private Profile userProfile;

  public PreferencesManager(IPlatformAdapter platformAdapter) {
    this.platformAdapter = platformAdapter;
    prefs = Gdx.app.getPreferences(PREFS_NAME);
    retrieveUserProfile();
    if (platformAdapter.supportsSpeech() && isSpeechEnabled()) {
      platformAdapter.provisionSpeech();
    }
    defaultInstallProfile = new InstallProfile();
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

  public boolean isSpeechEnabled() {
    return prefs.getBoolean(PREF_SPEECH_ENABLED, true);
  }

  public void setSpeechEnabled(boolean speechEnabled) {
    prefs.flush();
    prefs.putBoolean(PREF_SPEECH_ENABLED, speechEnabled);
    
    // if the speech is now enabled, provision it.
    if (speechEnabled) {
      platformAdapter.provisionSpeech();
    }
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
    prefs.putString(ProfileData.USER_ID, userProfile.getUserEmail());
    prefs.flush();
    this.userProfile = userProfile;
  }

  private Profile retrieveUserProfile() {
    String userId = getProfileUserId();
    userProfile = getUserProfile(userId);
    userProfile.setPlatform(platformAdapter.getPlatform());
    userProfile.setInstallationId(platformAdapter.getInstallationId());
    if (userProfile.getUserEmail().length() > 0) {
      prefs.putString(ProfileData.USER_ID, userProfile.getUserEmail());
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

  // The userid for currently active profile - useremail if available, else installid
  public String getProfileUserId() {
    String userId = prefs.getString(ProfileData.USER_ID);
    if (userId == null || userId.length() == 0) {
      userId = platformAdapter.getInstallationId();
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
        return defaultInstallProfile;
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
    String userId = getProfileUserId();
    if (mergeServerProfileIfAvailable(profile, userId)) {
      markProfileDirty(userId);
      Gdx.app.log(ScienceEngine.LOG, "Saved Profile - " + userId);
    }
  }

  /**
   * Merge profile of active user from the server, if it is available, into user's profile.
   * @param profile
   * @param userId
   * @return true iff server profile merge resulted in changes
   */
  public boolean mergeServerProfileIfAvailable(Profile profile, String userId) {
    String serverProfileBase64 = prefs.getString(SERVER_PROFILE_PREFIX + userId);
    if (serverProfileBase64 != null) {
      profile.mergeProfile(serverProfileBase64);
      prefs.remove(SERVER_PROFILE_PREFIX + userId);
    }
    String localProfileBase64 = profile.toBase64();

    String savedProfile = prefs.getString(userId);
    // No need to save if already up to date
    if (localProfileBase64.equals(savedProfile)) return false;
    
    prefs.putString(userId, localProfileBase64);
    return true;
  }

  private void markProfileDirty(String userId) {
    // Add to set of profiles which need to be synced to server
    String syncProfilesString = getSyncProfilesString();
    if (!syncProfilesString.startsWith(userId + "\n")) {
      setSyncProfilesString(userId + "\n" + syncProfilesString);
    }
    prefs.flush();
  }
  
  private static class SyncProfilesTask implements Runnable {

    private String syncProfilesString;
    private Preferences prefs;
    private InstallProfile installProfile;
    private IPlatformAdapter platformAdapter;
    
    public SyncProfilesTask(String syncProfilesString, Preferences prefs, IPlatformAdapter platformAdapter,
        InstallProfile installProfile) {
      this.prefs = prefs;
      this.syncProfilesString = syncProfilesString;
      this.installProfile = installProfile;
      this.platformAdapter = platformAdapter;
    }
    @Override
    public void run() {
      Map<String, String> postParams = new HashMap<String, String>();
      String[] syncProfilesArray = syncProfilesString.split("\n");
      HashSet<String> syncProfiles = new HashSet<String>(Arrays.asList(syncProfilesArray));
      syncInstallProfile(platformAdapter.getInstallationId());
      for (String userId: syncProfiles) {
        syncUserProfile(postParams, userId);
      }
    }
    
    private void setInstallProfile(InstallProfile installProfile) {
      this.installProfile = installProfile;
    }
    
    private void syncInstallProfile(String installId) {
      try {
        // Get back updated server installProfile, if any
        long lastUpdated = -1;
        if (installProfile != null) {
          lastUpdated = installProfile.getLastUpdated();
        }
        String installProfileBase64;
        if (installProfile.isChanged()) {
          Map<String, String> postParams = new HashMap<String, String>();
          postParams.put(ProfileData.INSTALL_ID, installId);
          installProfileBase64 = installProfile.toBase64();
          installProfileBase64 = platformAdapter.httpPost(ServerConstants.INSTALL_PROFILE_SERVLET,
              "application/octet-stream", postParams, installProfileBase64.getBytes());
          Gdx.app.log(ScienceEngine.LOG, "Sync Install Profile to MazaLearn - " + installId);
          installProfile.markChanged(false);
       } else {
          installProfileBase64 = platformAdapter.httpGet(ServerConstants.INSTALL_PROFILE_SERVLET + "?" + 
              ProfileData.INSTALL_ID + "=" + installId + "&" + 
              ProfileData.LAST_UPDATED + "=" + String.valueOf(lastUpdated));
        }
        InstallProfile newInstallProfile = InstallProfile.fromBase64((String) installProfileBase64);
        if (newInstallProfile == null) {
           Gdx.app.error(ScienceEngine.LOG, "Invalid or unchanged install profile");
        } else {
          prefs.putString(INSTALL_PROFILE, installProfileBase64);
          prefs.flush();
          // Load installProfile in PreferencesManager
          setInstallProfile(newInstallProfile);
        }
        Gdx.app.log(ScienceEngine.LOG, "Got Install Profile from MazaLearn: " + installId);
      } catch(GdxRuntimeException e) {
        if (ScienceEngine.DEV_MODE.isDebug()) e.printStackTrace();
        Gdx.app.log(ScienceEngine.LOG, "Network Problem: Failed to get - " + installId);
      }
    }

    private void syncUserProfile(Map<String, String> postParams, String userId) {
      String localProfileBase64 = prefs.getString(userId);
      // Resurrect profile and find data to send
      Profile profile = Profile.fromBase64(localProfileBase64);
      String syncProfileBase64 = profile.getSyncStr();
      postParams.put(ProfileData.USER_ID, userId);
      try {
        // Post userProfile to server and get back updated server userProfile
        String serverProfileBase64 =
            platformAdapter.httpPost(ServerConstants.USER_PROFILE_SERVLET, "application/octet-stream", 
                postParams, syncProfileBase64.getBytes());
        prefs.putString(SERVER_PROFILE_PREFIX + userId, serverProfileBase64);
        Gdx.app.log(ScienceEngine.LOG, "Sync Profile to MazaLearn - " + userId);
      } catch(GdxRuntimeException e) {
        if (ScienceEngine.DEV_MODE.isDebug()) e.printStackTrace();
        Gdx.app.log(ScienceEngine.LOG, "Network Problem: Failed to sync - " + userId);
      }
    }
    
  }

  public void syncProfiles(boolean forceSync) {
    if (!forceSync && "Manual".equals(prefs.getString(PREFS_SYNC_MODE))) return;
    
    Gdx.app.log(ScienceEngine.LOG, "Syncing Profiles");
    if (forceSync) {
      markProfileDirty(getProfileUserId());
    }
    String syncProfilesString = prefs.getString(SYNC_PROFILES);
    Gdx.app.log(ScienceEngine.LOG, "Sync Profile: " + syncProfilesString);
    if (syncProfilesString.length() == 0) return;
    this.installProfile = null; // so that it will get refreshed
    SyncProfilesTask syncProfilesTask = 
        new SyncProfilesTask(syncProfilesString, prefs, platformAdapter, getInstallProfile());
    platformAdapter.executeAsync(syncProfilesTask);
    setSyncProfilesString("");
  }

  public void saveInstallProfile() {
    prefs.putString(INSTALL_PROFILE, getInstallProfile().toBase64());
    prefs.flush();
  }

  public void setSync(String syncMode) {
    prefs.putString(PREFS_SYNC_MODE, syncMode);
  }
}
