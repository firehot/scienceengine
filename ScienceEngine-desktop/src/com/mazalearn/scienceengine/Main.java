package com.mazalearn.scienceengine;


import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "ScienceEngine";
		cfg.useGL20 = true;
		cfg.width = 800;
		cfg.height = 480;
		
		ScienceEngine scienceEngine = new ScienceEngine(args.length > 0 ? args[0] : "");
		//scienceEngine.setPlatformAdapter(new PlatformAdapterImpl());
    new LwjglApplication(scienceEngine, cfg) {
      @Override
      public void exit() {
        ScienceEngine.getProfileManager().persist();
        super.exit();
      }
    };
	}
}
