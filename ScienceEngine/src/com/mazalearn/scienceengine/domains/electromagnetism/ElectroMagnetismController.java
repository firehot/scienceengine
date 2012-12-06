package com.mazalearn.scienceengine.domains.electromagnetism;


import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.core.controller.AbstractScience2DController;
import com.mazalearn.scienceengine.core.model.AbstractScience2DModel;
import com.mazalearn.scienceengine.core.view.AbstractScience2DView;

/**
 * Electromagnetism Experiment
 */
public class ElectroMagnetismController extends AbstractScience2DController {
  
  public static final String NAME = "Electromagnetism";

  public ElectroMagnetismController(int level, int width, int height, Skin skin) {
    super(NAME, level, skin);
    AbstractScience2DModel emModel = new ElectroMagnetismModel();
    AbstractScience2DView emView = 
        new ElectroMagnetismView(width, height, emModel, skin, this);
    this.initialize(emModel,  emView);
  }
}
