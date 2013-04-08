package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Wire;

public class WireActor extends Science2DActor {
  private final Wire currentWire;
  private static TextureRegion currentWireDown = 
      ScienceEngine.getTextureRegion("currentwire-down");
  private static TextureRegion currentWireUp = 
      ScienceEngine.getTextureRegion("currentwire-up");;
    
  public WireActor(Science2DBody body) {
    super(body, new TextureRegion(currentWireUp));
    this.currentWire = (Wire) body;
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    this.setTextureRegion(
        currentWire.getCurrent() > 0 ? currentWireUp : currentWireDown);
    super.draw(batch, parentAlpha);
  }
}