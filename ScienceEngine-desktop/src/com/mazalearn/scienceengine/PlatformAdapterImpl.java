package com.mazalearn.scienceengine;

import java.io.File;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.designer.LevelEditor;

class PlatformAdapterImpl extends AbstractPlatformAdapter {
  
  IMessage messages;
  
  @Override
  public Platform getPlatform() {
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
  public void showURL(String url) {
    FileHandle file = Gdx.files.external(url);
    if (file.exists()) {
      String path = file.file().getAbsolutePath();
      browseURL("file:///" + path.replace("\\", "/"));
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
  public BitmapFont getFont(int pointSize) {
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
}