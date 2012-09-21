package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.box2d.Box2DActor;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.experiments.electromagnetism.ElectroMagnetismModel;
import com.mazalearn.scienceengine.experiments.electromagnetism.ElectroMagnetismModel.Mode;
import com.mazalearn.scienceengine.view.AbstractExperimentView;

public class ElectroMagnetView extends Box2DActor {
  private final AbstractExperimentView emView;
  private ElectroMagnetismModel emModel;
  
  public ElectroMagnetView(TextureRegion textureRegion, ScienceBody body, 
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