package com.mazalearn.scienceengine.experiments.electromagnetism;


import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.core.controller.AbstractScience2DController;
import com.mazalearn.scienceengine.core.view.AbstractScience2DStage;

/**
 * Electromagnetism Experiment
 */
public class ElectroMagnetismController extends AbstractScience2DController {
  
  public static final String NAME = "Electromagnetism";

  public ElectroMagnetismController(int width, int height, Skin skin) {
    super(NAME, skin);
    ElectroMagnetismModel emModel = new ElectroMagnetismModel();
    AbstractScience2DStage emView = 
        new ElectroMagnetismView(width, height, emModel, skin);
    this.initialize(emModel,  emView);
  }
}
