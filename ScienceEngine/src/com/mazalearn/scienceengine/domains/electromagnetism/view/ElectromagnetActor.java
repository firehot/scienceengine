package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.ElectroMagnet;

public class ElectromagnetActor extends Science2DActor {
  private static TextureRegion coil = 
      new TextureRegion(new Texture("images/electromagnet-coil.png"));
  private static int COIL_OFFSET = 38;
  private ElectroMagnet electromagnet;
  public ElectromagnetActor(Science2DBody body, TextureRegion textureRegion) {
    super(body, textureRegion);
    electromagnet = (ElectroMagnet) body;
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    // Add the additional loops
    for (int i = 1; i <= electromagnet.getNumberOfLoops(); i++) {
      batch.draw(coil, getX() - i * COIL_OFFSET, getY(), 0, 0, getWidth(), 
          getHeight(), 1, 1, getRotation());
    }
  }

  public Actor hit (float x, float y, boolean touchable) {
    if (touchable && getTouchable() != Touchable.enabled) return null;
    float coilWidth = electromagnet.getCoilWidth() * ScreenComponent.PIXELS_PER_M;
    return x >= - electromagnet.getNumberOfLoops() * coilWidth && x < getWidth() 
        && y >= 0 && y < getHeight() ? this : null;
  }

}