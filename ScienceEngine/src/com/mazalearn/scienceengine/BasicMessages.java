package com.mazalearn.scienceengine;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.app.services.IMessage;

final class BasicMessages implements IMessage {
  
  private Map<String,String> i18nMap = new HashMap<String, String>();

  public BasicMessages() {
    FileHandle file = Gdx.files.internal("data/Messages.properties");
    if (file.exists()) {
      String contents = file.readString("UTF8");
      String[] lines = contents.split("\n");
      for (String line: lines) {
        String[] keyval = line.split("=");
        if (keyval.length < 2) {
          Gdx.app.error(ScienceEngine.LOG, "Improper message line: " + line);
          continue; // Improper line.
        }
        i18nMap.put(keyval[0], keyval[1]);
      }
    }
  }
  
  @Override
  public String getString(String msg) {
    String s = i18nMap.get(msg);
    if (s != null) return s;
    
    int pos = msg.indexOf(".");
    return msg.substring(pos + 1);
  }

  @Override
  public String getLanguage() {
    return "en";
  }

  @Override
  public void setLanguage(Skin skin, String language) {
  }

  @Override
  public void setFont(Skin skin) {
  }
}