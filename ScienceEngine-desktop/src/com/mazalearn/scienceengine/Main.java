package com.mazalearn.scienceengine;

import java.util.Arrays;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.utils.UrlViewer;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "ScienceEngine";
		cfg.useGL20 = true;
		cfg.width = 800;
		cfg.height = 480;
		
		ScienceEngine scienceEngine = new ScienceEngine(Arrays.asList(args));
		scienceEngine.setUrlViewer(new UrlViewerImpl());
    new LwjglApplication(scienceEngine, cfg);
	}
	
	static class UrlViewerImpl implements UrlViewer {
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
	}
}
