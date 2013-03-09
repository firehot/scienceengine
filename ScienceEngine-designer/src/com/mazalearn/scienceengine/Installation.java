package com.mazalearn.scienceengine;

import java.io.FileWriter;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Installation {
  private static String sID = null;
  private static final String INSTALLATION = "INSTALLATION";

  public synchronized static String id() {
    if (sID != null) return sID;
    
    FileHandle installation = Gdx.files.external(INSTALLATION);
    try {
      if (!installation.exists()) {
        FileWriter out = new FileWriter(installation.file());
        out.write(UUID.randomUUID().toString());
        out.close();
      }
      sID = installation.readString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    return sID;
  }
}