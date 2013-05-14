package com.mazalearn.scienceengine;


import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.app.utils.IPlatformAdapter.Platform;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "ScienceEngine";
		cfg.useGL20 = true;
		// canonical: 800x480
		// nexus 7: 1280x800
		// ipad: 1024x768
		// iphone: 320x480
		// iphone 4: 640x960
		// iphone 5: 640x1136
		cfg.width = 480; // 800;
		cfg.height = 320; // 480;
		
    ScienceEngine.DEV_MODE = DevMode.DEBUG;
		ScienceEngine scienceEngine = new ScienceEngine(args.length > 0 ? args[0] : "");
		ScienceEngine.setPlatformAdapter(new PlatformAdapterImpl(Platform.Desktop));
    new LwjglApplication(scienceEngine, cfg) {
      @Override
      public void exit() {
        super.exit();
      }
    };
	}
}
