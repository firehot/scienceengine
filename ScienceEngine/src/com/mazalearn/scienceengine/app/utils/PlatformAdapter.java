package com.mazalearn.scienceengine.app.utils;

import java.io.File;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.core.controller.IScience2DController;

public interface PlatformAdapter {

  // Platform - required for utf-8/iso-8859 in characters
  enum Platform { Desktop, Android, GWT};
  
  public Platform getPlatform();
  
  // Show url in any browser
  public void browseURL(String url);
  
  // Show url in captive browser
  public void showURL(String uri);

  // Plays video corresponding to file. Returns true iff successful
  public boolean playVideo(File file);

  // Abstracting level editor out so that GWT is not affected by it
  public Stage createLevelEditor(IScience2DController science2dController,
      AbstractScreen screen);

}

