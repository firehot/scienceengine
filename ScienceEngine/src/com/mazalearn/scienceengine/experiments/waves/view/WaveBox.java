package com.mazalearn.scienceengine.experiments.waves.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mazalearn.scienceengine.experiments.waves.WaveModel.Ball;

public class WaveBox extends Actor {

  private final Texture backgroundTexture;
  private final TextureRegion ballTextureRed, ballTextureBlue;
  private final Ball[] balls;
  private final float originX, originY;
  
  public WaveBox(TextureRegion ballTextureRed, TextureRegion ballTextureBlue,
      Texture backgroundTexture, Ball[] balls, float originX, float originY) {
    super("WaveBox");
    this.ballTextureRed = ballTextureRed;
    this.ballTextureBlue = ballTextureBlue;
    this.backgroundTexture = backgroundTexture;
    this.balls = balls;
    this.originX = originX;
    this.originY = originY;
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    // Draw background
    batch.draw(backgroundTexture, this.x, this.y, this.width, this.height);
    // Draw the molecules
    int i = 1;
    for (Ball ball: balls) {
      i = (i + 1) % 10;
      batch.draw(i == 0 ? ballTextureBlue : ballTextureRed, 
          originX + ball.pos.x, originY + ball.pos.y);
    }
  }

  @Override
  public Actor hit(float x, float y) {
    return null;
  }
  
}