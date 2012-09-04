package com.mazalearn.scienceengine.experiments.electromagnetism;


import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.controller.AbstractExperimentController;
import com.mazalearn.scienceengine.view.AbstractExperimentView;

/**
 * Electromagnetism Experiment
 */
public class ElectroMagnetismController extends AbstractExperimentController {
  
  public ElectroMagnetismController(int width, int height, Skin skin) {
    super(skin);
    ElectroMagnetismModel emModel = new ElectroMagnetismModel();
    AbstractExperimentView emView = new ElectroMagnetismView(width, height, emModel);
    this.initialize(emModel,  emView);
  }
}
