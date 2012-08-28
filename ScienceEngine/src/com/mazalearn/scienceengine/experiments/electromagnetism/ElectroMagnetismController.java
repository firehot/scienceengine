package com.mazalearn.scienceengine.experiments.electromagnetism;


import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.controller.Configurator;

/**
 * Electromagnetism Experiment
 */
public class ElectroMagnetismController extends Table {
  
  public ElectroMagnetismController(Skin skin) {
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
  }
}
