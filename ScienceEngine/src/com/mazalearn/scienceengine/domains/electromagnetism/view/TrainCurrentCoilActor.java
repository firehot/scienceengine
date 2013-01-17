package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.CurrentCoil;

public class TrainCurrentCoilActor extends Science2DActor {
  private final CurrentCoil currentCoil;
   
  private static final int NUM_FRAMES = 36;

  TextureRegion[] rotationFrames;

  public TrainCurrentCoilActor(Science2DBody body, BitmapFont font) {
    super(body, null);
    this.currentCoil = (CurrentCoil) body;
    rotationFrames = new TextureRegion[NUM_FRAMES];
    TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("images/electromagnetism/currentcoil/pack.atlas"));
    for (int i = 0; i < NUM_FRAMES; i++) {
      String num = String.valueOf(i);
      rotationFrames[i] = atlas.findRegion("000".substring(0, 3 - num.length()) + num + '0');
    }
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    float rotation = getRotation();
    int frameIndex = (int) Math.floor(((rotation + 360) % 360 ) / 10);
    TextureRegion frame = rotationFrames[frameIndex];
    batch.draw(frame, (currentCoil.getPosition().x - currentCoil.getWidth() / 2 - 1) * ScienceEngine.PIXELS_PER_M, 
        (currentCoil.getPosition().y - currentCoil.getWidth() / 2 - 2.5f) * ScienceEngine.PIXELS_PER_M,
        frame.getRegionWidth()/2, frame.getRegionHeight()/2, getWidth()*1.2f, getWidth()*1.2f, 1, 1, 0); 
  }
}