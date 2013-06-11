package com.mazalearn.scienceengine.app.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter;
import com.mazalearn.scienceengine.app.utils.ServerConstants;

public class SyncProfilesTask implements Runnable {

  private static final long TIME_MS_BETWEEN_SYNCS = 3600 * 1000; // one hour
  private String syncProfilesString;
  private PreferencesManager prefs;
  private InstallProfile installProfile;
  private IPlatformAdapter platformAdapter;
  private static long lastSyncTimeMs = 0;
  
  public SyncProfilesTask(String syncProfilesString, PreferencesManager prefs, IPlatformAdapter platformAdapter,
      InstallProfile installProfile) {
    this.prefs = prefs;
    this.syncProfilesString = syncProfilesString;
    this.installProfile = installProfile;
    this.platformAdapter = platformAdapter;
  }
  
  @Override
  public void run() {
    lastSyncTimeMs = System.currentTimeMillis();
    Map<String, String> postParams = new HashMap<String, String>();
    String[] syncProfilesArray = syncProfilesString.split("\n");
    HashSet<String> syncProfiles = new HashSet<String>(Arrays.asList(syncProfilesArray));
    syncInstallProfile(platformAdapter.getInstallationId());
    for (String userId: syncProfiles) {
      syncUserProfile(postParams, userId);
    }
  }
  
  public static boolean isTimeToSync(InstallProfile installProfile) {
    long now = System.currentTimeMillis();
    return installProfile.isChanged() || now - lastSyncTimeMs > TIME_MS_BETWEEN_SYNCS;
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
        // Load installProfile in PreferencesManager
        prefs.setInstallProfile(newInstallProfile);
      }
      Gdx.app.log(ScienceEngine.LOG, "Got Install Profile from MazaLearn: " + installId);
    } catch(GdxRuntimeException e) {
      if (ScienceEngine.DEV_MODE.isDebug()) e.printStackTrace();
      Gdx.app.log(ScienceEngine.LOG, "Network Problem: Failed to get - " + installId);
    }
  }

  private void syncUserProfile(Map<String, String> postParams, String userId) {
    Profile profile = prefs.getUserProfile(userId);
    String syncProfileBase64 = profile.getSyncStr();
    postParams.put(ProfileData.USER_ID, userId);
    try {
      // Post userProfile to server and get back updated server userProfile
      String serverProfileBase64 =
          platformAdapter.httpPost(ServerConstants.USER_PROFILE_SERVLET, "application/octet-stream", 
              postParams, syncProfileBase64.getBytes());
      prefs.setServerProfile(userId, serverProfileBase64);
      Gdx.app.log(ScienceEngine.LOG, "Sync Profile to MazaLearn - " + userId);
    } catch(GdxRuntimeException e) {
      if (ScienceEngine.DEV_MODE.isDebug()) e.printStackTrace();
      Gdx.app.log(ScienceEngine.LOG, "Network Problem: Failed to sync - " + userId);
    }
  }

  public static void setSyncAfterSeconds(int seconds) {
    lastSyncTimeMs = System.currentTimeMillis() - TIME_MS_BETWEEN_SYNCS + seconds * 1000;
  }

}