package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Magnet;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Magnet.MagnetType;

public class MagnetActor extends Science2DActor {
  private final Magnet magnet;
  private static Texture ferrite = 
      new Texture("images/ferrite.png");
  private static Texture smco = 
      new Texture("images/smco.png");;
  private static Texture neodymium = 
          new Texture("images/neodymium.png");
    
  
  public MagnetActor(Science2DBody body, TextureRegion textureRegion) {
    super(body, textureRegion);
    this.magnet = (Magnet) body;
  }
  
  @Override
  public void act(float delta) {
    super.act(delta);
    float width = magnet.getWidth() * ScienceEngine.PIXELS_PER_M;
    float height = magnet.getHeight() * ScienceEngine.PIXELS_PER_M;
    this.setWidth(width);
    this.setHeight(height);
    this.setOrigin(width/2, height/2);
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    Texture texture = null;
    switch(MagnetType.valueOf(magnet.getMagnetType())) {
    case Ferrite: texture = ferrite; break;
    case Smco: texture = smco; break;
    case Neodymium: texture = neodymium; break;
    }
    this.getTextureRegion().setTexture(texture);
    super.draw(batch, parentAlpha);
  }
}