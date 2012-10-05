package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.CurrentCoil;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.CurrentWire;

public class CurrentCoilActor extends Science2DActor {
  private final CurrentCoil currentCoil;
  private static Texture currentWireDown = 
      new Texture("images/currentwire-down.png");
  private static Texture currentWireUp = 
      new Texture("images/currentwire-up.png");;
    
  public CurrentCoilActor(Science2DBody body, TextureRegion textureRegion) {
    super(body, textureRegion);
    this.currentCoil = (CurrentCoil) body;
    this.setAllowMove(true);
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
//    this.getTextureRegion().setTexture(
//        currentWire.getCurrent() < 0 ? currentWireUp : currentWireDown);
    super.draw(batch, parentAlpha);
  }
}