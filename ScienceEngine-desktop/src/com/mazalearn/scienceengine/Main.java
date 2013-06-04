package com.mazalearn.scienceengine;


import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter.Platform;

public class Main {
  public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "ScienceEngine";
		cfg.useGL20 = true;
		Device device = Device.IPad;
		cfg.width = device.width;
		cfg.height = device.height;
		
    ScienceEngine.DEV_MODE.setDebug(true);
    ScienceEngine.DEV_MODE.setDummyBilling(true);
		ScienceEngine scienceEngine = new ScienceEngine(args.length > 0 ? args[0] : "", device);
		ScienceEngine.setPlatformAdapter(new DesktopPlatformAdapter(Platform.Desktop));
    new LwjglApplication(scienceEngine, cfg) {
      @Override
      public void exit() {
        super.exit();
      }
    };
	}
}
