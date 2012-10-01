package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.PickupCoil;

public class PickupCoilActor extends Science2DActor {
  private static TextureRegion coil = 
      new TextureRegion(new Texture("images/coppercoils-front2.png"));
  private static int COIL_OFFSET = 45;
  private PickupCoil pickupCoil;
  public PickupCoilActor(Science2DBody body, TextureRegion textureRegion) {
    super(body, textureRegion);
    pickupCoil = (PickupCoil) body;
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    // Add the additional loops
    for (int i = 1; i <= pickupCoil.getNumberOfLoops(); i++) {
      batch.draw(coil, getX() + i * COIL_OFFSET, getY(), 0, 0, getWidth(), 
          getHeight(), 1, 1, getRotation());
    }
  }
}