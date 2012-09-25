package com.mazalearn.scienceengine.experiments.electromagnetism;


import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.app.services.SoundManager;
import com.mazalearn.scienceengine.core.controller.AbstractExperimentController;
import com.mazalearn.scienceengine.core.view.AbstractExperimentView;

/**
 * Electromagnetism Experiment
 */
public class ElectroMagnetismController extends AbstractExperimentController {
  
  public static final String NAME = "Electromagnetism";

  public ElectroMagnetismController(int width, int height, Skin skin) {
    super(NAME, skin);
    ElectroMagnetismModel emModel = new ElectroMagnetismModel();
    AbstractExperimentView emView = 
        new ElectroMagnetismView(width, height, emModel, skin);
    this.initialize(emModel,  emView);
  }
}
