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
		cfg.width = 1024; // 800;
		cfg.height = 768; // 480;
		
		ScienceEngine scienceEngine = new ScienceEngine(args.length > 0 ? args[0] : "");
		scienceEngine.setPlatformAdapter(new PlatformAdapterImpl(Platform.Desktop));
		ScienceEngine.DEV_MODE = DevMode.PRODUCTION;
    new LwjglApplication(scienceEngine, cfg) {
      @Override
      public void exit() {
        super.exit();
      }
    };
	}
}
