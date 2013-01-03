package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.ScienceEngine;
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
  private BitmapFont font;
  private Vector2 newPos = new Vector2();
   
    
  public CurrentCoilActor(Science2DBody body, BitmapFont font) {
    super(body, commutatorNone);
    this.currentCoil = (CurrentCoil) body;
    this.font = font;
  }
  

  @Override
  protected int getRotationForceScaler() {
    return -100;
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
    drawRotationData(batch, parentAlpha);
  }
  
  private void drawRotationData(SpriteBatch batch, float parentAlpha) {
    font.setColor(0f, 0f, 0f, parentAlpha);
    int data = Math.round(currentCoil.getRotationData());
    String rotationData = String.valueOf(data);
    newPos.set(currentCoil.getWorldCenter()).mul(ScienceEngine.PIXELS_PER_M);
    // Create space for text - in screen coords and always left to right
    newPos.add(-5, 5);
    font.draw(batch, rotationData, newPos.x, newPos.y);
  }
}