package com.mazalearn.scienceengine.experiments.waves;


import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.controller.Configurator;

/**
 * Wave Motion experimentModel
 */
public class WaveController extends Table {
  
  private static final int NUM_BALLS = 40;
  private static final int BALL_DIAMETER = 8 * 4;

  public WaveController(TextureAtlas atlas, Skin skin) {
    super(skin);
    if (ScienceEngine.DEV_MODE) {
      debug();
    }
    this.setFillParent(true);
    final WaveModel waveModel = new WaveModel(NUM_BALLS, BALL_DIAMETER);
    final WaveView waveView = new WaveView(1400, 960, waveModel, NUM_BALLS, BALL_DIAMETER, atlas);
    this.add(waveView).fill();
    Configurator configurator = new Configurator(skin, waveModel, waveView);
    this.add(configurator).width(300).height(960).fill();
  }

}
