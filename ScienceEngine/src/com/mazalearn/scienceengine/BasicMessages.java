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
  
    TextButtonStyle style1 = skin.get(TextButtonStyle.class);
    style1.font = font;
    style1 = skin.get("toggle", TextButtonStyle.class);
    style1.font = font;
    style1 = skin.get("body", TextButtonStyle.class);
    style1.font = font;
    style1 = skin.get("mcq", TextButtonStyle.class);
    style1.font = font;
    LabelStyle style2 = skin.get(LabelStyle.class);
    style2.font = font;
    CheckBoxStyle style3 = skin.get(CheckBoxStyle.class);
    style3.font = font;
    SelectBoxStyle style4 = skin.get(SelectBoxStyle.class);
    style4.font = font;
    TextField.TextFieldStyle style5 = skin.get(TextField.TextFieldStyle.class);
    style5.font = font;
    skin.add("default-font", font);
  }
}