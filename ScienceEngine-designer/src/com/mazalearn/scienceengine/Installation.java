package com.mazalearn.scienceengine;

import java.util.Random;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.mazalearn.scienceengine.app.utils.Crypter;

public class Installation {
  private static String sID = null;
  private static final String INSTALLATION = "data/INSTALLATION";
  public synchronized static String id(String platform, String deviceId) {
    if (sID != null) return sID;
    
    FileHandle installation = Gdx.files.external(INSTALLATION);
    try {
      Gdx.app.log(ScienceEngine.LOG, "Checking for installation file");
      if (!installation.exists()) {
      	Gdx.app.log(ScienceEngine.LOG, "Creating installation file");
      	// Following does not work on IOS - mono. Hence the weaker workaround.
        // String id = platform + "-" + UUID.randomUUID().toString();
      	
        Random r = new Random();
        UUID uuid = new UUID(r.nextLong(), r.nextLong());
        String sId = platform + "-" + uuid.toString();
        sId = sId.toLowerCase();
        String hash = Crypter.saltedSha1Hash(sId, deviceId);
        String id = sId + "-" + hash;
        Gdx.app.log(ScienceEngine.LOG, "Installation id: " + id);
        installation.writeBytes(id.getBytes(), false);
      }
  	  Gdx.app.log(ScienceEngine.LOG, "Reading installation file");     
      String id = installation.readString();
      // Validate that installation file belongs to this device - if not, delete
      sID = id.substring(0, platform.length() + 1 + 36);
      String hash = Crypter.saltedSha1Hash(sID, deviceId);
      int len = platform.length() + 2 + 36;
      if (id.length() <= len || !hash.equals(id.substring(len))) {
        installation.delete();
        throw new IllegalStateException("Invalid installation: Deleting");
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    return sID;
  }
}