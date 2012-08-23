package com.mazalearn.scienceengine.experiments;


import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.experiments.controller.Configurator;
import com.mazalearn.scienceengine.experiments.model.ElectroMagnetismModel;
import com.mazalearn.scienceengine.experiments.view.ElectroMagnetismView;
import com.mazalearn.scienceengine.experiments.model.ElectroMagnetismModel.Mode;

/**
 * Electromagnetism Experiment
 */
public class ElectroMagnetismExperiment extends Table {
  
  public ElectroMagnetismExperiment(Skin skin) {
    super(skin);
    if (ScienceEngine.DEV_MODE) {
      debug();
    }
    this.setFillParent(true);
    ElectroMagnetismModel emModel = new ElectroMagnetismModel();
    ElectroMagnetismView emView = new ElectroMagnetismView(400, 200, emModel);
    this.add(emView).fill();
    Configurator configurator = new Configurator(skin, emModel, emView);
    this.add(configurator).width(30).fill();
    configurator.addSelect("Mode", 
        new String[] {Mode.Free.name(), Mode.Rotate.name()});
  }
}
