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
    configurator.add("EndType", new String[] {"FixedEnd", "LooseEnd", "NoEnd"});
    configurator.add("GenMode", new String[] {"Oscillate", "Pulse"});
    configurator.add("Tension", 1, 10);
    configurator.add("Damping", 0, 0.5f);
    configurator.add("PulseWidth", 5, 20);
    configurator.add("Frequency", 0, 1);
    configurator.add("Amplitude", 0, 10);
  }

}
