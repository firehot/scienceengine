package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Monopole;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Monopole.MonopoleType;

public class MonopoleActor extends Science2DActor {
  private final Monopole monopole;
  private TextureRegion textureSouthPole;
  private TextureRegion textureNorthPole;
    
  public MonopoleActor(Science2DBody body, TextureRegion textureRegion) {
    super(body, textureRegion);
    this.monopole = (Monopole) body;
    this.textureNorthPole = ScienceEngine.getTextureRegion("northpole");
    this.textureSouthPole = ScienceEngine.getTextureRegion("southpole");
  }
  
  @Override
  public void act(float delta) {
    super.act(delta);
    // Wrap around
    if (getX() < 0 || getX() > ScreenComponent.VIEWPORT_WIDTH || 
        getY() < 0 || getY() > ScreenComponent.VIEWPORT_HEIGHT) {
      setX((getX() + ScreenComponent.VIEWPORT_WIDTH) % ScreenComponent.VIEWPORT_WIDTH);
      setY((getY() + ScreenComponent.VIEWPORT_HEIGHT) % ScreenComponent.VIEWPORT_HEIGHT);
      this.setPositionFromViewCoords(false);
    }
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    this.setTextureRegion(
        monopole.getPoleType() == MonopoleType.NorthPole ? textureNorthPole : textureSouthPole);
    super.draw(batch, parentAlpha);
  }
}