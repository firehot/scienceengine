package com.mazalearn.scienceengine.experiments;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;

/**
 * Wave Motion experiment
 */
public class WaveMotion extends Table {
  
  public WaveMotion(Skin skin) {
    super(skin);
    if (ScienceEngine.DEV_MODE) {
      debug();
    }
    this.add(new Rope(100,150));
  }

}
