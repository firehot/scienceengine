package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.CurrentCoil;

public class CurrentCoilActor extends Science2DActor {
  private final CurrentCoil currentCoil;
  private static TextureRegion commutatorNone = 
      new TextureRegion(new Texture("images/currentcoil_nocommutator.png"));
  private static TextureRegion commutatorAc = 
      new TextureRegion(new Texture("images/currentcoil_accommutator.png"));
  private static TextureRegion commutatorDc = 
      new TextureRegion(new Texture("images/currentcoil_dccommutator.png"));
    
    
  public CurrentCoilActor(Science2DBody body) {
    super(body, commutatorNone);
    this.currentCoil = (CurrentCoil) body;
    this.setAllowMove(true);
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    // Flip if negative current so that +, - on coil are correctly shown
    TextureRegion textureRegion = null;
    float rotation = getRotation();
    rotation += currentCoil.getCurrent() < 0 ? 180 : 0;
    switch(currentCoil.getCommutatorType()) {
    case Commutator: 
      textureRegion = commutatorDc;
      break;
    case Connector:
      textureRegion = commutatorAc;
      break;
    case Disconnected:
      textureRegion = commutatorNone;
    }
    batch.draw(textureRegion, getX(), getY(), this.getOriginX(), 
        this.getOriginY(), getWidth(), getHeight(), 1, 1, rotation);
  }
}