package com.mazalearn.scienceengine.domains.waves;


import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.core.controller.AbstractScience2DController;

/**
 * Wave Motion science2DModel
 */
public class WaveController extends AbstractScience2DController {
  
  public static final String NAME = "Waves";
  private static final int NUM_BALLS = 40;

  public WaveController(int level, int width, int height, TextureAtlas atlas, Skin skin) {
    super(NAME, level, skin);
    final WaveModel waveModel = new WaveModel(NUM_BALLS);
    final WaveView waveView = 
        new WaveView(width, height, waveModel, skin, atlas);
    initialize(waveModel, waveView);
  }

}
