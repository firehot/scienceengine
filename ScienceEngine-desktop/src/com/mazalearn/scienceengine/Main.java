package com.mazalearn.scienceengine;

import java.io.File;
import java.io.IOException;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.app.utils.PlatformAdapter;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.designer.LevelEditor;

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
	}
}
