package com.mazalearn.scienceengine.experiments;


import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.experiments.controller.Condition;
import com.mazalearn.scienceengine.experiments.controller.Configurator;
import com.mazalearn.scienceengine.experiments.model.WaveModel;

/**
 * Electromagnetism Experiment
 */
public class ElectromagnetismExperiment extends Table {
  
  public ElectromagnetismExperiment(Skin skin) {
    super(skin);
    if (ScienceEngine.DEV_MODE) {
      debug();
    }
    this.setFillParent(true);
    final WaveModel waveModel = new WaveModel(600, 380);
    this.add(waveModel).fill();
    Configurator configurator = new Configurator(skin, waveModel);
    this.add(configurator).width(30).fill();
  }
}
