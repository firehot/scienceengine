package com.mazalearn.scienceengine.experiments.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.mazalearn.scienceengine.experiments.model.ElectroMagnetismModel;

public class ElectroMagnetismView extends Group implements IExperimentView {
  private boolean isPaused = false;
  
  private final ElectroMagnetismModel emModel;
  
  public ElectroMagnetismView(float width, float height, final ElectroMagnetismModel emModel) {
    this.width = width;
    this.height = height;
    this.emModel = emModel;
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    // Advance n steps
    if (!isPaused ) {
    }
    super.draw(batch, parentAlpha);
  }

  @Override
  public void pause() {
    this.isPaused = true;
  }

  @Override
  public void resume() {
    this.isPaused = false;
  }

  @Override
  public boolean isPaused() {
    return isPaused;
  }
}
