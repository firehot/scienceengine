package com.mazalearn.scienceengine;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap.Entry;
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
    BitmapFont smallFont = null;
    BitmapFont bigFont = null;
    String language = getLanguage();
    try {
      font = skin.getFont(language);
      smallFont = skin.getFont(language + "-small");
      bigFont = skin.getFont(language + "-big");
    } catch (GdxRuntimeException e) { // font not found
      font = skin.getFont("en"); // Supports only english
      smallFont = skin.getFont("en-small");
      bigFont = skin.getFont("en-big");
    }
    skin.add(language, font);
      
    for (Entry<String, TextButtonStyle> nameStyle: skin.getAll(TextButtonStyle.class).entries()) {
      String key = nameStyle.key;
      nameStyle.value.font = key.equals("default-small") ? smallFont : key.equals("default-big") ? bigFont : font;
    }
    for (Entry<String, LabelStyle> nameStyle: skin.getAll(LabelStyle.class).entries()) {
      String key = nameStyle.key;
      nameStyle.value.font = key.equals("default-small") ? smallFont : key.equals("default-big") ? bigFont : font;
    }
    for (Entry<String, CheckBoxStyle> nameStyle: skin.getAll(CheckBoxStyle.class).entries()) {
      String key = nameStyle.key;
      nameStyle.value.font = key.equals("default-small") ? smallFont : key.equals("default-big") ? bigFont : font;
    }
    for (Entry<String, SelectBoxStyle> nameStyle: skin.getAll(SelectBoxStyle.class).entries()) {
      String key = nameStyle.key;
      nameStyle.value.font = key.equals("default-small") ? smallFont : key.equals("default-big") ? bigFont : font;
    }
    for (Entry<String, TextField.TextFieldStyle> nameStyle: skin.getAll(TextField.TextFieldStyle.class).entries()) {
      String key = nameStyle.key;
      nameStyle.value.font = key.equals("default-small") ? smallFont : key.equals("default-big") ? bigFont : font;
    }
    for (Entry<String, ListStyle> nameStyle: skin.getAll(ListStyle.class).entries()) {
      String key = nameStyle.key;
      nameStyle.value.font = key.equals("default-small") ? smallFont : key.equals("default-big") ? bigFont : font;
    }
    skin.add("default-font", font);
    skin.add("default-small", smallFont);
    skin.add("default-big", bigFont);
  }
}