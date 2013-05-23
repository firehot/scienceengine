package com.mazalearn.scienceengine.app.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.SerializationException;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.utils.Crypter;

/**
 * The installation profile.
 * <p>
 * This class is used to store the profile of the installation, and is persisted to the file
 * system when the scienceEngine exits.
 * 
 */
public class InstallProfile implements Serializable {

  private InstallData data = new InstallData();

  public InstallProfile() {
    data.installId = ScienceEngine.getPlatformAdapter().getInstallationId();
  }

    // Serializable implementation

  @Override
  public void read(Json json, OrderedMap<String, Object> jsonData) {

    data = json.readValue("data", InstallData.class, jsonData);
    if (data == null) {
      data = new InstallData();
      data.installId = ScienceEngine.getPlatformAdapter().getInstallationId();
    }
  }

  @Override
  public void write(Json json) {
    json.writeValue("data", data);
  }

  public String getEnterpriseName() {
    return data.enterpriseName == null ? "" : data.enterpriseName;
  }

  public String getEnterpriseId() {
    return data.enterpriseId == null ? "" : data.enterpriseId;
  }
  
  public String getRegisteredUserId() {
    return data.registeredUserId == null ? "" : data.registeredUserId;
  }

  public void save() {
    data.lastUpdated = System.currentTimeMillis();
    ScienceEngine.getPreferencesManager().saveInstallProfile();
  }
  
  public long getLastUpdated() {
    return data.lastUpdated;
  }

  public Pixmap getEnterpriseLogo() {
    String png = data.pngEnterpriseLogo;
    return png == null ? null : ScienceEngine.getPlatformAdapter().bytes2Pixmap(Base64Coder.decode(png));
  }

  public String[] getUserIds() {    
    return data.userIds;
  }

  public String getInstallationId() {
    return data.installId;
  }

  public String getInstallationName() {
    return data.installName;
  }
  
  public boolean isAvailableTopic(Topic topic) {
    return data.availableTopicNames.contains(topic.name());
  }

  /**
   * Only method which modifies installation profile data on client side.
   * @param topic
   */
  public void addAsAvailableTopic(Topic topic) {
    data.availableTopicNames.add(topic.name());
    data.isChanged = true;
  }
  
  public static InstallProfile fromBase64(String profileBase64AndHash) {
    InstallProfile installProfile = null;
    if (profileBase64AndHash != null && profileBase64AndHash.length() > 0) {
      // decode the contents - hash is last 40 bytes
      String profileBase64 = profileBase64AndHash.substring(0, profileBase64AndHash.length() - 40);
      String hashReceived = profileBase64AndHash.substring(profileBase64AndHash.length() - 40);
      // Verify hash
      String installId = ScienceEngine.getPlatformAdapter().getInstallationId();
      String hashCalculated = Crypter.saltedSha1Hash(profileBase64, installId);
      if (!hashCalculated.equals(hashReceived)) {
        Gdx.app.error(ScienceEngine.LOG, "Install profile - Hash mismatch: " + hashCalculated + " " + hashReceived);
        return null;
      }
      String profileJson = Base64Coder.decodeString(profileBase64);
      try {
        installProfile = new InstallProfile();
        installProfile.data = new Json(OutputType.javascript).fromJson(InstallData.class, profileJson);
        // verify the installid
        if (!installId.toLowerCase().equals(installProfile.getInstallationId().toLowerCase())) {
          Gdx.app.error(ScienceEngine.LOG, "Install profile - Install id mismatch");
          return null;
        }
        
      } catch (SerializationException s) {
        Gdx.app.error(ScienceEngine.LOG, "Install Profile - Error deserializing: " + s.getMessage() + "\n" + profileJson);
      } catch (IllegalArgumentException s) {
        Gdx.app.error(ScienceEngine.LOG, "Install Profile - Error deserializing: " + s.getMessage() + "\n" + profileJson);
      }
    }
    return installProfile;
  }

  public String toBase64() {
    String profileAsText = new Json(OutputType.javascript).toJson(data);
    Gdx.app.log(ScienceEngine.LOG, "Saving InstallProfile - " + profileAsText);

    String profileBase64 = Base64Coder.encodeString(profileAsText);
    String hash = Crypter.saltedSha1Hash(profileBase64, ScienceEngine.getPlatformAdapter().getInstallationId());
    return profileBase64 + hash;
  }

  public boolean isChanged() {
    return data.isChanged;
  }

  void markChanged(boolean changed) {
    data.isChanged = changed;
    
  }

}
