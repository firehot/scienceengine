package com.mazalearn.scienceengine;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.designer.LevelEditor;

class PlatformAdapterImpl extends AbstractPlatformAdapter {
  
  IMessage messages;
  
  @Override
  public Platform getPlatform() {
    // return Gdx.app.getType();
    return Platform.Desktop;
  }
  
  @Override
	public void browseURL(String url) {
    if(java.awt.Desktop.isDesktopSupported() ) {
      java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
      
      if(desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
        try {
          java.net.URI uri = new java.net.URI(url);
          desktop.browse(uri);
        }
        catch ( Exception e ) {
          System.err.println( e.getMessage() );
        }
      }
    }
	}

  @Override
  public boolean playVideo(File file) {
    if(java.awt.Desktop.isDesktopSupported() ) {
      java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
      
      if(desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
        try {
          desktop.open(file);
          return true;
        }
        catch ( Exception e ) {
          System.err.println( e.getMessage() );
          try {
            Runtime.getRuntime().exec("cmd.exe /C \"" + file.getAbsolutePath() + "\"");
            return true;
          } catch (IOException e2) {
            System.err.println( e2.getMessage() );
          }
        }
      }
    }
    return false;
  }


  @Override
  public Stage createLevelEditor(IScience2DController science2DController,
      AbstractScreen screen) {
    return new LevelEditor(science2DController, screen);
  }
  
  @Override
  public IMessage getMsg() {
    if (messages == null) {
      this.messages = new Messages(Platform.Desktop);
    }
    return messages;
  }

  @Override
  public BitmapFont getScaledFont(int pointSize) {
    FileHandle fontFileHandle = Gdx.files.internal("skin/Roboto-Regular.ttf");
    StringBuilder characters = new StringBuilder();
    for (char c = 0; c <= 127; c++) {
      characters.append(c);
    }
    FreeTypeFontGenerator generator = 
        new FreeTypeFontGenerator(fontFileHandle);
    BitmapFont font = generator.generateFont(pointSize, characters.toString(), false);
    generator.dispose();
    return font;
  }

  private static final String HINDI_TTF = "Lohit-Devanagari.ttf"; // "aksharhindi.ttf";
  private static final String KANNADA_TTF = "Lohit-Kannada.ttf"; // "aksharkannada.ttf";
  @Override
  public BitmapFont loadFont(Skin skin, String language) {
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
    FreeTypeFontGenerator generator = 
        new FreeTypeFontGenerator(fontFileHandle);
    StringBuilder characters = new StringBuilder();
    for (char c = beginChar; c <= endChar; c++) {
      characters.append(c);
    }
    font = generator.generateFont(16, characters.toString(), false);
    generator.dispose();      

    return font;
  }
  
  @Override
  public void getBytes(Pixmap pixmap, byte[] lines) {
    ByteBuffer pixels = pixmap.getPixels();
    pixels.get(lines);
  }

  @Override
  public void setBytes(Pixmap pixmap, byte[] lines) {
    ByteBuffer pixels = pixmap.getPixels();
    pixels.clear();
    pixels.put(lines);
    pixels.clear();   
  }
}