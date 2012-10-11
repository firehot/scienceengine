package com.mazalearn.scienceengine;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.utils.PlatformAdapter;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "ScienceEngine";
		cfg.useGL20 = true;
		cfg.width = 800;
		cfg.height = 480;
		
		ScienceEngine scienceEngine = new ScienceEngine(args.length > 0 ? args[0] : "");
		scienceEngine.setPlatformAdapter(new PlatformAdapterImpl());
    new LwjglApplication(scienceEngine, cfg) {
      @Override
      public void exit() {
        ScienceEngine.getProfileManager().persist();
        super.exit();
      }
    };
	}
	
	static class PlatformAdapterImpl implements PlatformAdapter {
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
      browseURL(url);
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
	}
}
