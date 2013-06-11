package com.mazalearn.scienceengine.app.services;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;

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

  public void setSoundEnabled(boolean soundEnabled) {
    prefs.putBoolean(PREF_SOUND_ENABLED, soundEnabled);
    userProfile.setSoundEnabled(soundEnabled);
    prefs.flush();
  }

  public boolean isMusicEnabled() {
    return prefs.getBoolean(PREF_MUSIC_ENABLED, true);
  }

  public void setMusicEnabled(boolean musicEnabled) {
    prefs.putBoolean(PREF_MUSIC_ENABLED, musicEnabled);
    userProfile.setMusicEnabled(musicEnabled);
    prefs.flush();
  }

  public boolean isSpeechEnabled() {
    return prefs.getBoolean(PREF_SPEECH_ENABLED, true);
  }

  public void setSpeechEnabled(boolean speechEnabled) {
    prefs.flush();
    prefs.putBoolean(PREF_SPEECH_ENABLED, speechEnabled);
    userProfile.setSpeechEnabled(speechEnabled);
    
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

  void setInstallProfile(InstallProfile installProfile) {
    this.installProfile = installProfile;
    saveInstallProfile();
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

  void setServerProfile(String userId, String serverProfileBase64) {
    prefs.putString(PreferencesManager.SERVER_PROFILE_PREFIX + userId, serverProfileBase64);
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
  
  public void syncProfiles(boolean forceSync) {
    if (!forceSync && "Manual".equals(prefs.getString(PREFS_SYNC_MODE))) return;
    
    Gdx.app.log(ScienceEngine.LOG, "Syncing Profiles");
    if (forceSync) {
      markProfileDirty(getProfileUserId());
    }
    String syncProfilesString = prefs.getString(SYNC_PROFILES);
    Gdx.app.log(ScienceEngine.LOG, "Sync Profile: " + syncProfilesString);
    
    if (syncProfilesString.length() == 0 || !SyncProfilesTask.isTimeToSync(getInstallProfile())) return;

    SyncProfilesTask syncProfilesTask = 
        new SyncProfilesTask(syncProfilesString, this, platformAdapter, getInstallProfile());
    platformAdapter.executeAsync(syncProfilesTask);
    setSyncProfilesString("");
  }

  void saveInstallProfile() {
    prefs.putString(INSTALL_PROFILE, getInstallProfile().toBase64());
    prefs.flush();
  }

  public void setSync(String syncMode) {
    prefs.putString(PREFS_SYNC_MODE, syncMode);
  }

  public void setSyncAfterSeconds(int seconds) {
    SyncProfilesTask.setSyncAfterSeconds(seconds);
  }
}
