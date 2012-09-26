package com.mazalearn.scienceengine.app.utils;

import java.io.File;

public interface ResourceViewer {

  // Show url in browser
  public void browseURL(String url);

  // Plays video corresponding to file. Returns true iff successful
  public boolean playVideo(File file);

}

