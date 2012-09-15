package com.mazalearn.scienceengine.experiments.electromagnetism;


import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.controller.AbstractExperimentController;
import com.mazalearn.scienceengine.services.SoundManager;
import com.mazalearn.scienceengine.view.AbstractExperimentView;

/**
 * Electromagnetism Experiment
 */
public class ElectroMagnetismController extends AbstractExperimentController {
  
  public static final String NAME = "Electromagnetism";

  public ElectroMagnetismController(int width, int height, Skin skin, 
      SoundManager soundManager) {
    super(NAME, skin);
    ElectroMagnetismModel emModel = new ElectroMagnetismModel();
    AbstractExperimentView emView = 
        new ElectroMagnetismView(NAME, width, height, emModel, skin, soundManager);
    this.initialize(emModel,  emView);
  }
}
