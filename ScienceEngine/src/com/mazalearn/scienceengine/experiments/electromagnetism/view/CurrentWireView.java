package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.box2d.Box2DActor;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.CurrentWire;

public class CurrentWireView extends Box2DActor {
  private final CurrentWire currentWire;
  private float radius;
  private static Texture currentWireDown = 
      new Texture("images/currentwire-down.png");
  private static Texture currentWireUp = 
      new Texture("images/currentwire-up.png");;
    
  public CurrentWireView(ScienceBody body) {
    super(body, new TextureRegion(currentWireUp));
    this.radius = (float) Math.sqrt(width * width + height * height)/2;
    this.currentWire = (CurrentWire) body;
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    this.getTextureRegion().setTexture(
        currentWire.isDirectionUp() ? currentWireUp : currentWireDown);
    batch.draw(getTextureRegion(), x - width/2, y - height/2, 0, 0, width, 
        height, 1, 1, rotation);
  }
}