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
  private float ballDiameter;
  
  public WaveBox(TextureRegion ballTextureRed, TextureRegion ballTextureBlue,
      Texture backgroundTexture, Ball[] balls, float originX, float originY,
      float ballDiameter) {
    super();
    super.setName("Wavebox");
    this.ballTextureRed = ballTextureRed;
    this.ballTextureBlue = ballTextureBlue;
    this.backgroundTexture = backgroundTexture;
    this.balls = balls;
    this.originX = originX;
    this.originY = originY;
    this.ballDiameter = ballDiameter;
    this.setWidth(this.ballDiameter * (balls.length + 10));
    this.setHeight(this.ballDiameter * 20);
    this.setX(0);
    this.setY(0);
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    // Draw background
    batch.draw(backgroundTexture, this.getX(), this.getY(), 
        this.getWidth(), this.getHeight());
    // Draw the molecules
    int i = 1;
    for (Ball ball: balls) {
      i = (i + 1) % 10;
      batch.draw(i == 0 ? ballTextureBlue : ballTextureRed, 
          getX() + (originX + ball.pos.x) * ballDiameter,
          getY() + (originY + ball.pos.y) * ballDiameter);
    }
  }

  public float getBallDiameter() {
    return ballDiameter;
  }
}