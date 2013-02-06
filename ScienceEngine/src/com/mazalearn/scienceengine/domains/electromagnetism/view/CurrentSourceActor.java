package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.CurrentSource;

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
    float scaledCurrent = currentSource.getCurrent() / CurrentSource.DEFAULT_MAX_CURRENT;
    float rotation = scaledCurrent >= 0 ? 90 : 270;
    float width = ScreenComponent.getScaledX(current.getRegionWidth() * Math.abs(scaledCurrent));
    float height = ScreenComponent.getScaledY(current.getRegionHeight());
    batch.draw(current, getX() + this.getWidth() * 0.7f - height/2, 
        getY() + this.getHeight()/2.35f, 0, height/2, width, height, 
        1, 1, rotation);
  }
}