package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.box2d.ScienceActor;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.CurrentWire;

public class CurrentWireView extends ScienceActor {
  private final CurrentWire currentWire;
  private static Texture currentWireDown = 
      new Texture("images/currentwire-down.png");
  private static Texture currentWireUp = 
      new Texture("images/currentwire-up.png");;
    
  public CurrentWireView(ScienceBody body) {
    super(body, new TextureRegion(currentWireUp));
    this.currentWire = (CurrentWire) body;
    this.setAllowDrag(true);
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    this.getTextureRegion().setTexture(
        currentWire.isDirectionUp() ? currentWireUp : currentWireDown);
    super.draw(batch, parentAlpha);
  }
}