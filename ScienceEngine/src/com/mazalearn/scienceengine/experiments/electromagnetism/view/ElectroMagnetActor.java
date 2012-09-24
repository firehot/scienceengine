package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.core.model.ScienceBody;
import com.mazalearn.scienceengine.core.view.AbstractExperimentView;
import com.mazalearn.scienceengine.core.view.ScienceActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.ElectroMagnetismModel;
import com.mazalearn.scienceengine.experiments.electromagnetism.ElectroMagnetismModel.Mode;

public class ElectroMagnetActor extends ScienceActor {
  private final AbstractExperimentView emView;
  private ElectroMagnetismModel emModel;
  
  public ElectroMagnetActor(TextureRegion textureRegion, ScienceBody body, 
      AbstractExperimentView experimentView, ElectroMagnetismModel emModel) {
    super(body, textureRegion);
    this.emView = experimentView;
    this.emModel = emModel;
    this.setAllowDrag(true);
  }

  public void touchDragged(float x, float y, int pointer) {
    if (Mode.valueOf(emModel.getMode()) != Mode.Free) return;
    super.touchDragged(x, y, pointer);
    emView.resume();
  }
}