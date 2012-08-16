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
    this.setFillParent(true);
    final WaveString waveString = new WaveString(600, 380);
    this.add(waveString).fill();
    Configurator configurator = new Configurator(skin, waveString);
    this.add(configurator).width(30).fill();
    configurator.addSelect("EndType", new String[] {"FixedEnd", "LooseEnd", "NoEnd"});
    configurator.addSelect("GenMode", new String[] {"Oscillate", "Pulse" /*, "Manual" */});
    configurator.addSlider("Tension", 1, 10);
    configurator.addSlider("Damping", 0, 0.5f);
    configurator.addSlider("PulseWidth", 5, 20);
    configurator.addSlider("Frequency", 0, 1);
    configurator.addSlider("Amplitude", 0, 100);
  }

}
