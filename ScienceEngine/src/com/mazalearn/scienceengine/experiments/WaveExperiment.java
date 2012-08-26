package com.mazalearn.scienceengine.experiments;


import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.experiments.controller.Configurator;
import com.mazalearn.scienceengine.experiments.model.WaveModel;
import com.mazalearn.scienceengine.experiments.view.WaveView;

/**
 * Wave Motion iExperimentModel
 */
public class WaveExperiment extends Table {
  
  private static final int NUM_BALLS = 40;
  private static final int BALL_DIAMETER = 8;

  @SuppressWarnings("unchecked")
  public WaveExperiment(TextureAtlas atlas, Skin skin) {
    super(skin);
    if (ScienceEngine.DEV_MODE) {
      debug();
    }
    this.setFillParent(true);
    final WaveModel waveModel = new WaveModel(NUM_BALLS, BALL_DIAMETER);
    final WaveView waveView = new WaveView(600, 380, waveModel, NUM_BALLS, BALL_DIAMETER, atlas);
    this.add(waveView).fill();
    Configurator configurator = new Configurator(skin, waveModel, waveView);
    this.add(configurator).width(30).fill();
    configurator.addSelect(waveModel.getConfig("EndType"));
    configurator.addSelect(waveModel.getConfig("GenMode"));
    configurator.addSlider(waveModel.getConfig("Tension"));
    configurator.addSlider(waveModel.getConfig("Damping"));
    configurator.addSlider(waveModel.getConfig("PulseWidth"));
    configurator.addSlider(waveModel.getConfig("Frequency"));
    configurator.addSlider(waveModel.getConfig("Amplitude"));
  }

}
