package com.mazalearn.scienceengine;

import java.util.Locale;
import java.util.ResourceBundle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.FreeTypeComplexFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.app.utils.PlatformAdapter.Platform;

public class Messages implements IMessage {
  private static final String HINDI_TTF = "Lohit-Devanagari.ttf"; // "aksharhindi.ttf";
  private static final String KANNADA_TTF = "Lohit-Kannada.ttf"; // "aksharkannada.ttf";
  private static final String BUNDLE_NAME = "com.mazalearn.scienceengine.data.Messages"; //$NON-NLS-1$

  private Locale locale = new Locale("en");
  private Platform platform;
  private ResourceBundle resourceBundle = 
      ResourceBundle.getBundle(BUNDLE_NAME, locale);
  
  public Messages(Platform platform) {
    this.platform = platform;
  }

  public String getString(String key) {
    try {
      String val = resourceBundle.getString(key);
      return platform == Platform.Android ? val : new String(val.getBytes("ISO-8859-1"), "UTF-8");
    } catch (Exception e) {
      e.printStackTrace();
      return '!' + key + '!';
    }
  }
  
  public String getLanguage() {
    return locale.getLanguage();
  }

  public void setLanguage(Skin skin, String language) {
    locale = new Locale(language);
    resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
    setFont(skin);
  }

  public void setFont(Skin skin) {
    BitmapFont font = null;
    String language = locale.getLanguage();
    try {
      font = skin.getFont(language);
    } catch (GdxRuntimeException e) { // font not found
      font = loadFont(skin, language);
    }
    skin.add(language, font);
  
    TextButtonStyle style1 = skin.get(TextButtonStyle.class);
    style1.font = font;
    style1 = skin.get("toggle", TextButtonStyle.class);
    style1.font = font;
    LabelStyle style2 = skin.get(LabelStyle.class);
    style2.font = font;
    CheckBoxStyle style3 = skin.get(CheckBoxStyle.class);
    style3.font = font;
    SelectBoxStyle style4 = skin.get(SelectBoxStyle.class);
    style4.font = font;
    skin.add("default-font", font);
  }

  private BitmapFont loadFont(Skin skin, String language) {
    BitmapFont font;
    String fontFileName = null;
    char beginChar = 0, endChar = 0;
    if (language.equals("ka")) {
      fontFileName = KANNADA_TTF; // unicode: 0C80-0CFF
      beginChar = '\u0c80'; endChar = '\u0cff';
    } else if (language.equals("hi")) {
      fontFileName =  HINDI_TTF; // unicode: 0900-097F
      beginChar = '\u0900'; endChar = '\u097f';
    }
    BitmapFontCache.setFallbackFont(skin.getFont("en"));
    FileHandle fontFileHandle = Gdx.files.internal("skin/" + fontFileName);
    if (platform == Platform.Android || platform == Platform.AndroidEmulator) {
      BitmapFontCache.setComplexScriptLayout(language, fontFileName);
      FreeTypeComplexFontGenerator generator = 
          new FreeTypeComplexFontGenerator(fontFileHandle);
      font = generator.generateFont(16, false);
      generator.dispose();
    } else {
      FreeTypeFontGenerator generator = 
          new FreeTypeFontGenerator(fontFileHandle);
      StringBuilder characters = new StringBuilder();
      for (char c = beginChar; c <= endChar; c++) {
        characters.append(c);
      }
      font = generator.generateFont(16, characters.toString(), false);
      generator.dispose();      
    }
    return font;
  }
}
