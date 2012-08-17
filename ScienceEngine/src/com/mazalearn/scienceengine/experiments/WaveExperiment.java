package com.mazalearn.scienceengine.experiments;


import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.experiments.controller.Condition;
import com.mazalearn.scienceengine.experiments.controller.Configurator;
import com.mazalearn.scienceengine.experiments.model.WaveModel;
import com.mazalearn.scienceengine.experiments.view.WaveView;

/**
 * Wave Motion experimentModel
 */
public class WaveExperiment extends Table {
  
  private static final int NUM_BALLS = 40;
  private static final int BALL_DIAMETER = 8;

  public WaveExperiment(Skin skin) {
    super(skin);
    if (ScienceEngine.DEV_MODE) {
      debug();
    }
    this.setFillParent(true);
    final WaveModel waveModel = new WaveModel(NUM_BALLS, BALL_DIAMETER);
    final WaveView waveView = new WaveView(600, 380, waveModel, NUM_BALLS, BALL_DIAMETER);
    this.add(waveView).fill();
    Configurator configurator = new Configurator(skin, waveModel, waveView);
    this.add(configurator).width(30).fill();
    configurator.addSelect("EndType", new String[] {"FixedEnd", "LooseEnd", "NoEnd"});
    configurator.addSelect("GenMode", new String[] {"Oscillate", "Pulse", "Manual"});
    configurator.addSlider("Tension", 1, 10);
    configurator.addSlider("Damping", 0, 0.5f);
    configurator.addSlider("PulseWidth", 5, 20).addCondition(new Condition() {
      @Override
      public boolean eval() {
        return waveModel.getGenMode() == "Pulse";
      }
    });
    configurator.addSlider("Frequency", 0, 1).addCondition(new Condition() {
      @Override
      public boolean eval() {
        return waveModel.getGenMode() == "Oscillate";
      }
    });;
    configurator.addSlider("Amplitude", 0, 100).addCondition(new Condition() {
      @Override
      public boolean eval() {
        return waveModel.getGenMode() != "Manual";
      }
    });;
  }

}
