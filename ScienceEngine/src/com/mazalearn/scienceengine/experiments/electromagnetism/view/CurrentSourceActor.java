package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.CurrentSource;

public class CurrentSourceActor extends Science2DActor {
  private static TextureRegion current = 
      new TextureRegion(new Texture("images/current.png"));
  private CurrentSource currentSource;
  public CurrentSourceActor(Science2DBody body, TextureRegion textureRegion) {
    super(body, textureRegion);
    currentSource = (CurrentSource) body;
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    float scaledCurrent = currentSource.getCurrent() / currentSource.getMaxCurrent();
    float rotation = scaledCurrent >= 0 ? 90 : 270;
    float width = current.getRegionWidth() * Math.abs(scaledCurrent);
    int height = current.getRegionHeight();
    batch.draw(current, getX() + this.getWidth() - height/2, 
        getY() + this.getHeight()/3.5f, 0, height/2, width, height, 
        1, 1, rotation);
  }
}