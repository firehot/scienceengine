package com.mazalearn.scienceengine.app.utils;

import java.io.File;

public interface ResourceViewer {

  // Platform - required for utf-8/iso-8859 in characters
  enum Platform { Desktop, Android, GWT};
  
  public Platform getPlatform();
  
  // Show url in browser
  public void browseURL(String url);

  // Plays video corresponding to file. Returns true iff successful
  public boolean playVideo(File file);

}

