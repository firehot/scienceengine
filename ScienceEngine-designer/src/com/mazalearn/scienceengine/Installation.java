package com.mazalearn.scienceengine;

import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Installation {
  private static String sID = null;
  private static final String INSTALLATION = "data/INSTALLATION";

  public synchronized static String id() {
    if (sID != null) return sID;
    
    FileHandle installation = Gdx.files.external(INSTALLATION);
    try {
      Gdx.app.log(ScienceEngine.LOG, "Checking for installation file");
      if (!installation.exists()) {
      	Gdx.app.log(ScienceEngine.LOG, "Creating installation file");
        String id = ScienceEngine.getPlatformAdapter().getPlatform() + "-" + UUID.randomUUID().toString();
        installation.writeBytes(id.getBytes(), false);
      }
  	  Gdx.app.log(ScienceEngine.LOG, "Reading installation file");     
      sID = installation.readString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    return sID;
  }
}