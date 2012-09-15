package com.mazalearn.scienceengine.experiments.waves;


import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.controller.AbstractExperimentController;
import com.mazalearn.scienceengine.services.SoundManager;

/**
 * Wave Motion experimentModel
 */
public class WaveController extends AbstractExperimentController {
  
  public static final String NAME = "Waves";
  private static final int NUM_BALLS = 40;

  public WaveController(int width, int height, TextureAtlas atlas, Skin skin, 
      SoundManager soundManager) {
    super(NAME, skin);
    final WaveModel waveModel = new WaveModel(NUM_BALLS);
    final WaveView waveView = 
        new WaveView(NAME, width, height, waveModel, skin, soundManager, atlas);
    initialize(waveModel, waveView);
  }

}
