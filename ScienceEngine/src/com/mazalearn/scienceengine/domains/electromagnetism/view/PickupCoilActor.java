package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.PickupCoil;

public class PickupCoilActor extends Science2DActor {
  private static TextureRegion coil = 
      new TextureRegion(ScienceEngine.assetManager.get("images/coppercoils-front2.png", Texture.class));
  private static int COIL_OFFSET = 35;
  private PickupCoil pickupCoil;
  public PickupCoilActor(PickupCoil pickupCoil, TextureRegion textureRegion) {
    super(pickupCoil, textureRegion);
    this.pickupCoil = pickupCoil;
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    // Add the additional loops
    for (int i = 1; i <= pickupCoil.getNumberOfLoops(); i++) {
      batch.draw(coil, getX() + 10 + (i - 1) * COIL_OFFSET, getY(), 0, 0, getWidth(), 
          getHeight(), 1, 1, getRotation());
    }
  }

  public Actor hit (float x, float y, boolean touchable) {
    if (touchable && getTouchable() != Touchable.enabled) return null;
    return x >= getWidth() / 2 && x < getWidth() + (pickupCoil.getNumberOfLoops() - 1) * COIL_OFFSET 
        && y >= 0 && y < getHeight() ? this : null;
  }
}