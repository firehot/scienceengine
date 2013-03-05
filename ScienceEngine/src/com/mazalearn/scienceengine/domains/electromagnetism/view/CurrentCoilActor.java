package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.CurrentCoil;

public class CurrentCoilActor extends Science2DActor {
  private final CurrentCoil currentCoil;
  private static TextureRegion commutatorNone = 
      ScienceEngine.getTextureRegion("currentcoil_nocommutator");
  private static TextureRegion commutatorAc = 
      ScienceEngine.getTextureRegion("currentcoil_accommutator");
  private static TextureRegion commutatorDc = 
      ScienceEngine.getTextureRegion("currentcoil_dccommutator");
  private BitmapFont font;
  private Vector2 newPos = new Vector2();
   
  private static final int NUM_FRAMES = 36;
  // To synchronize the coil commutator with blender animation.
  private int[] rotationAngles = new int[] { 0, 1, 7, 15, 28, 40, 55, 65, 80,
      90, 100, 110, 120, 135, 145, 155, 165, 174, 180, 187, 195, 203, 215, 225,
      240, 250, 260, 270, 280, 290, 305, 318, 330, 343, 352, 0 };

  TextureRegion[] rotationFrames;

  public CurrentCoilActor(Science2DBody body, BitmapFont font) {
    super(body, commutatorNone);
    this.currentCoil = (CurrentCoil) body;
    this.font = font;
/**
 * in directory electromangetism
 * java -cp c:\Users\sridhar\gdx-tools.jar;c:\Users\sridhar\git\scienceengi
ne\ScienceEngine\libs\gdx.jar com.badlogic.gdx.tools.imagepacker.TexturePacker2
currentcoil
 */
    rotationFrames = new TextureRegion[NUM_FRAMES];
    TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("images/Electromagnetism/currentcoil/pack.atlas"));
    for (int i = 0; i < NUM_FRAMES; i++) {
      String num = String.valueOf(i);
      rotationFrames[i] = atlas.findRegion("000".substring(0, 3 - num.length()) + num + '0');
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
    // 6, 8.5 for desktop 1280x1024 (1.6, 1.6)
    // 3.5, 5.5 for ipad 1024x768   (1.28, 1.2)
    // 1, 2.5 for 800x640           (1, 1)
    batch.draw(frame,
        (currentCoil.getPosition().x - currentCoil.getWidth() / 2 - 3.5f) * ScreenComponent.PIXELS_PER_M, 
        (currentCoil.getPosition().y - currentCoil.getWidth() / 2 - 6.5f) * ScreenComponent.PIXELS_PER_M,
        ScreenComponent.getScaledX(frame.getRegionWidth()/2), 
        ScreenComponent.getScaledY(frame.getRegionHeight()/2),
        getWidth()*1.2f, getWidth()*1.2f, 1, 1, 0); 

    int rotation2 = rotationAngles[frameIndex];
    batch.draw(textureRegion, getX(), getY(), this.getOriginX(), 
        this.getOriginY(), getWidth(), getHeight(), 1, 1, rotation2);
    drawRotationData(batch, parentAlpha);
  }
  
  private void drawRotationData(SpriteBatch batch, float parentAlpha) {
    font.setColor(0f, 0f, 0f, parentAlpha);
    int data = Math.round(currentCoil.getRotationData());
    String rotationData = String.valueOf(data);
    newPos.set(currentCoil.getWorldCenter()).mul(ScreenComponent.PIXELS_PER_M);
    // Create space for text - in screen coords and always left to right
    newPos.add(-5, 5);
    font.draw(batch, rotationData, newPos.x, newPos.y);
  }
}