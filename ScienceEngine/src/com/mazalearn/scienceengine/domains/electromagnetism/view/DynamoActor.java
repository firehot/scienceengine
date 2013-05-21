package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Dynamo;
import com.mazalearn.scienceengine.domains.electromagnetism.model.Dynamo.AreaOrientation;

public class DynamoActor extends Science2DActor {
  private final Dynamo dynamo;
   
  private static final int NUM_FRAMES = 36;

  private static final float COIL_OFFSET_PERCENT = 0.07f;

  TextureRegion[] rotationFrames;

  public DynamoActor(Science2DBody body, TextureRegion textureRegion) {
    super(body, textureRegion);
    this.dynamo = (Dynamo) body;
    rotationFrames = new TextureRegion[NUM_FRAMES];
    TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("images/electromagnetism/currentcoil/pack.atlas"));
    for (int i = 0; i < NUM_FRAMES; i++) {
      String num = String.valueOf(i);
      rotationFrames[i] = atlas.findRegion("000".substring(0, 3 - num.length()) + num + '0');
    }
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    float width = dynamo.getWidth() * ScreenComponent.PIXELS_PER_M;
    this.setWidth(width);
    this.setHeight(width);
    this.setOrigin(width/2, width/2);
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    if (AreaOrientation.valueOf(dynamo.getAreaOrientation()) == AreaOrientation.PerpendicularToRotation) {
      float rotation = getRotation();
      // Add the loops
      float perLoopRotation = 180f / dynamo.getNumberOfLoops();
      for (int i = 1; i <= dynamo.getNumberOfLoops(); i++) {
        int frameIndex = (int) Math.floor(((rotation + 360 + i * perLoopRotation) % 360 ) / 10);
        TextureRegion frame = rotationFrames[frameIndex];
        batch.draw(frame, (dynamo.getPosition().x - dynamo.getWidth() * 1.5f / 2) * ScreenComponent.PIXELS_PER_M, 
            (dynamo.getPosition().y - dynamo.getHeight() * 1.6f / 2) * ScreenComponent.PIXELS_PER_M,
            0, 0, getWidth() * 1.5f, getWidth() * 1.5f, 1, 1, 0);
      }
    } else { // Parallel to rotation
      // Draw the loops in reverse order
      float coilOffset = COIL_OFFSET_PERCENT * getWidth();
      for (int i = (int) dynamo.getNumberOfLoops() - 1; i >= 0; i--) {
        batch.draw(getTextureRegion(), getX() + 10 + (i - 1) * coilOffset, 
            getY(), getOriginX(), getOriginY(), getWidth(), 
            getHeight(), 1, 1, getRotation());
      }
    }
  }
}