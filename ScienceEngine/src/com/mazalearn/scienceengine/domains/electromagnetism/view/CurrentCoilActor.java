package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
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
   
  private static final int        FRAME_COLS = 36;
  private static final int        FRAME_ROWS = 1;
  // To synchronize the coil commutator with blender animation.
  private int[] rotationAngles = new int[] {
    0, 1, 7, 15, 28, 40, 55, 65, 80, 90, 100, 110, 120, 135, 145, 155, 165,
    174, 180, 187, 195, 203, 215, 225, 240, 250, 260, 270, 280, 290, 305, 318,
    330, 343, 352, 0
  };
  
  TextureRegion[]                 rotationFrames;
     
  public CurrentCoilActor(Science2DBody body, BitmapFont font) {
    super(body, commutatorNone);
    this.currentCoil = (CurrentCoil) body;
    this.font = font;

    Texture rotationSheet = new Texture("images/currentcoilsheet.png"); 
    TextureRegion[][] tmp = TextureRegion.split(rotationSheet, 
        rotationSheet.getWidth() / FRAME_COLS, rotationSheet.getHeight() / FRAME_ROWS);
    rotationFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
    int index = 0;
    for (int i = 0; i < FRAME_ROWS; i++) {
            for (int j = 0; j < FRAME_COLS; j++) {
                    rotationFrames[index++] = tmp[i][j];
            }
    }
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
    int frameIndex = (int) Math.floor(((rotation + 360) % 360 ) / 10);
    TextureRegion frame = rotationFrames[frameIndex];
    batch.draw(frame, (currentCoil.getPosition().x - currentCoil.getWidth() / 2 + 1) * ScienceEngine.PIXELS_PER_M, 
        (currentCoil.getPosition().y - currentCoil.getWidth() / 2 - 1.5f) * ScienceEngine.PIXELS_PER_M); 

    int rotation2 = rotationAngles[frameIndex];
    batch.draw(textureRegion, getX(), getY(), this.getOriginX(), 
        this.getOriginY(), getWidth(), getHeight(), 1, 1, rotation2);
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