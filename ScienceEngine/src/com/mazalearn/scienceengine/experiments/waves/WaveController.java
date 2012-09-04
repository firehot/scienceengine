package com.mazalearn.scienceengine.experiments.waves;


import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.controller.AbstractExperimentController;

/**
 * Wave Motion experimentModel
 */
public class WaveController extends AbstractExperimentController {
  
  private static final int NUM_BALLS = 40;
  private static final int BALL_DIAMETER = 8 * 4;

  public WaveController(int width, int height, TextureAtlas atlas, Skin skin) {
    super(skin);
    final WaveModel waveModel = new WaveModel(NUM_BALLS, BALL_DIAMETER);
    final WaveView waveView = new WaveView(height, width, waveModel, BALL_DIAMETER, atlas);
    initialize(waveModel, waveView);
  }

}
