package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Magnet;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Magnet.MagnetType;

public class MagnetActor extends Science2DActor {
  private final Magnet magnet;
  private static TextureRegion ferrite = ScienceEngine.getTextureRegion("ferrite");
  private static TextureRegion smco = ScienceEngine.getTextureRegion("smco");;
  private static TextureRegion neodymium = ScienceEngine.getTextureRegion("neodymium");
    
  
  public MagnetActor(Science2DBody body, TextureRegion textureRegion) {
    super(body, textureRegion);
    this.magnet = (Magnet) body;
  }
  
  @Override
  public void act(float delta) {
    super.act(delta);
    float width = magnet.getWidth() * ScreenComponent.PIXELS_PER_M;
    float height = magnet.getHeight() * ScreenComponent.PIXELS_PER_M;
    this.setWidth(width);
    this.setHeight(height);
    this.setOrigin(width/2, height/2);
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    TextureRegion textureRegion = null;
    switch(MagnetType.valueOf(magnet.getMagnetType())) {
    case Ferrite: textureRegion = ferrite; break;
    case Smco: textureRegion = smco; break;
    case Neodymium: textureRegion = neodymium; break;
    }
    this.setTextureRegion(textureRegion);
    super.draw(batch, parentAlpha);
  }
}