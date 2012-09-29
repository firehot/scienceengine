package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.CurrentSource;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Electromagnet;

public class ElectromagnetActor extends Science2DActor {
  private static TextureRegion coil = 
      new TextureRegion(new Texture("images/electromagnet-coil.png"));
  private static int COIL_OFFSET = 38;
  private Electromagnet electromagnet;
  public ElectromagnetActor(Science2DBody body, TextureRegion textureRegion) {
    super(body, textureRegion);
    electromagnet = (Electromagnet) body;
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    // Add the additional loops
    for (int i = 1; i <= electromagnet.getNumberOfLoops(); i++) {
      batch.draw(coil, x - i * COIL_OFFSET, y, 0, 0, width, height, 1, 1, rotation);
    }
  }
}