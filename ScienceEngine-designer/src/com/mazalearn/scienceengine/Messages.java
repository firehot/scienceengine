package com.mazalearn.scienceengine;

import java.util.Locale;
import java.util.ResourceBundle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.app.utils.FreeTypeFontGenerator;
import com.mazalearn.scienceengine.app.utils.PlatformAdapter.Platform;

public class Messages implements IMessage {
  private static final String HINDI_TTF = "aksharhindi.ttf";
  private static final String KANNADA_TTF = "aksharkannada.ttf";
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
      String fontFileName = null;
      String chars = null;
      char beginChar = '\0', endChar = '\0';
      if (language.equals("ka")) {
        fontFileName = KANNADA_TTF;
        beginChar = '\u0C80';
        endChar = '\u0CFF';
      } else if (language.equals("hi")) {
          fontFileName =  HINDI_TTF;
          beginChar = '\u0900';
          endChar = '\u097F';
      }
      if (platform == Platform.Android || platform == Platform.AndroidEmulator) {
        BitmapFontCache.setComplexScriptLayout(language, fontFileName);
      }
      FileHandle fontFileHandle = Gdx.files.internal("skin/" + fontFileName);
      FreeTypeFontGenerator generator = 
          new FreeTypeFontGenerator(fontFileHandle);
      chars = FreeTypeFontGenerator.DEFAULT_CHARS;
      StringBuilder s = new StringBuilder("*+-/");
      for (char c = beginChar; c <= endChar; c++) {
        s.append(c);
      }
      chars += s;
      StringBuffer dedupChars = new StringBuffer();
      for (int i = 0; i < chars.length(); i++) {
        if (chars.indexOf(chars.charAt(i)) == i) {
          dedupChars.append(chars.charAt(i));
        }
      }
      font = generator.generateFont(16, dedupChars.toString(), false);
      generator.dispose();
    }
    skin.add(language, font);
  
    TextButtonStyle style1 = skin.get(TextButtonStyle.class);
    style1.font = font;
    skin.add("default", style1);
    LabelStyle style2 = skin.get(LabelStyle.class);
    style2.font = font;
    CheckBoxStyle style3 = skin.get(CheckBoxStyle.class);
    style3.font = font;
    skin.add("default-font", font);
  }
}
