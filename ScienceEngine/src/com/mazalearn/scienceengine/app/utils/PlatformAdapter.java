package com.mazalearn.scienceengine.app.utils;

import java.io.File;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.core.controller.IScience2DController;

public interface PlatformAdapter {

  // Platform - required for utf-8/iso-8859 in characters
  enum Platform { Desktop, Android, AndroidEmulator, GWT, IOS};
  
  // Show url in any browser
  public void browseURL(String url);
  
  // Show internet url in captive browser
  public void showExternalURL(String uri);

  // Show local filesystem url in captive browser
  public void showInternalURL(String uri);

  // Plays video corresponding to file. Returns true iff successful
  public boolean playVideo(File file);

  // Abstracting level editor out so that GWT is not affected by it
  public Stage createLevelEditor(IScience2DController science2dController,
      AbstractScreen screen);

  // Return the i18n message adapter
  public IMessage getMsg();

  // return platform for this adapter
  public Platform getPlatform();

  // Scale font: Possible to do better with Freetype on some platforms
  public BitmapFont getScaledFont(int pointSize);

  // New font load not possible without Freetype support.
  public BitmapFont loadFont(Skin skin, String language);

  public void getBytes(Pixmap pixmap, byte[] lines);

  public void setBytes(Pixmap pixmap, byte[] lines);

  public byte[] getPngBytes(Pixmap snapshot);
}

