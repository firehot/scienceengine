package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.CurrentCoil;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.CurrentWire;

public class CurrentCoilActor extends Science2DActor {
  private final CurrentCoil currentCoil;
    
  public CurrentCoilActor(Science2DBody body, TextureRegion textureRegion) {
    super(body, textureRegion);
    this.currentCoil = (CurrentCoil) body;
    this.setAllowMove(true);
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    // Flip if negative current so that +, - on coil are correctly shown
    float rotation = getRotation() + (currentCoil.getCurrent() < 0 ? 180 : 0);
    batch.draw(getTextureRegion(), getX(), getY(), this.getOriginX(), 
        this.getOriginY(), getWidth(), getHeight(), 1, 1, rotation);
  }
}