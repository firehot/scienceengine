package com.mazalearn.scienceengine;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mazalearn.scienceengine.app.services.IMessage;

class BasicMessages implements IMessage {
  
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
        String value = keyval[1].replace("\\n", "\n"); // Newlines
        i18nMap.put(keyval[0], value);
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
    BitmapFont font = null;
    String language = getLanguage();
    try {
      font = skin.getFont(language);
    } catch (GdxRuntimeException e) { // font not found
      font = skin.getFont("en"); // Supports only english
    }
    skin.add(language, font);
  
    
    for (TextButtonStyle style: skin.getAll(TextButtonStyle.class).values()) {
      style.font = font;
    }
    for (LabelStyle style: skin.getAll(LabelStyle.class).values()) {
      style.font = font;
    }
    for (CheckBoxStyle style: skin.getAll(CheckBoxStyle.class).values()) {
      style.font = font;
    }
    for (SelectBoxStyle style: skin.getAll(SelectBoxStyle.class).values()) {
      style.font = font;
    }
    for (TextField.TextFieldStyle style: skin.getAll(TextField.TextFieldStyle.class).values()) {
      style.font = font;
    }
    skin.add("default-font", font);
  }
}