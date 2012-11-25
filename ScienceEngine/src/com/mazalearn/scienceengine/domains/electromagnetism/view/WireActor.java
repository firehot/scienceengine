package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Wire;

public class WireActor extends Science2DActor {
  private final Wire currentWire;
  private static Texture currentWireDown = 
      new Texture("images/currentwire-down.png");
  private static Texture currentWireUp = 
      new Texture("images/currentwire-up.png");;
    
  public WireActor(Science2DBody body) {
    super(body, new TextureRegion(currentWireUp));
    this.currentWire = (Wire) body;
    this.setAllowMove(true);
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    this.getTextureRegion().setTexture(
        currentWire.getCurrent() < 0 ? currentWireUp : currentWireDown);
    super.draw(batch, parentAlpha);
  }
}