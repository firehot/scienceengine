package com.mazalearn.scienceengine.app.utils;

import java.io.File;
import java.util.Map;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.tutor.IDoneCallback;

public interface IPlatformAdapter {

  // Platform - required for utf-8/iso-8859 in characters
  public enum Platform { Desktop, Android, AndroidEmulator, GWT, IOS};
  
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

  // New font load not possible without Freetype support.
  public BitmapFont loadFont(Skin skin, String language);

  public void getBytes(Pixmap pixmap, byte[] lines);

  public void putBytes(Pixmap pixmap, byte[] lines);

  public byte[] pixmap2Bytes(Pixmap snapshot);

  public Pixmap bytes2Pixmap(byte[] pngBytes);
  
  // Does platform support language change?
  public boolean supportsLanguage();

  public String httpPost(String path, String contentType, Map<String, String> params,
      byte[] data);

  public void takeSnapshot(Stage stage, Topic topicArea, Topic level, int x, int y,
      int width, int height);
  
  // Get unique ID for this installation
  public String getInstallationId();

  public String httpGet(String path);

  public void executeAsync(Runnable runnable);

  // Does platform support sync to server of install and user profiles?
  public boolean supportsSync();

  void launchPurchaseFlow(String sku, String itemType,
      IDoneCallback doneCallback, String extraData);
}

