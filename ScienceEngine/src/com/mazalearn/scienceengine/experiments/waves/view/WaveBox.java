package com.mazalearn.scienceengine.experiments.waves.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mazalearn.scienceengine.experiments.waves.WaveModel.Ball;
import com.mazalearn.scienceengine.experiments.waves.WaveView;

public class WaveBox extends Actor {

  private final TextureRegion ballTextureRed, ballTextureBlue;
  private final Ball[] balls;
  private final float originX, originY;
  private int ballDiameter;
  private WaveView waveView;
  
  public WaveBox(TextureRegion ballTextureRed, TextureRegion ballTextureBlue,
      Ball[] balls, float originX, float originY,
      WaveView waveView) {
    super();
    super.setName("Wavebox");
    this.waveView = waveView;
    this.ballTextureRed = ballTextureRed;
    this.ballTextureBlue = ballTextureBlue;
    this.balls = balls;
    this.originX = originX;
    this.originY = originY;
    super.setHeight(waveView.getHeight());
    this.setX(0);
    this.setY(0);
  }
  
  @Override
  public void setWidth(float width) {
    if (getWidth() == width) return;
    
    super.setWidth(width);
    this.ballDiameter = (int) (getWidth() / (balls.length + 10));
    waveView.setBallDiameter(ballDiameter);
  }
  
  public int getBallDiameter() {
    return ballDiameter;
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    // Draw the balls
    int i = 1;
    for (Ball ball: balls) {
      i = (i + 1) % 10;
      batch.draw(i == 0 ? ballTextureBlue : ballTextureRed, 
          getX() + (originX + ball.pos.x) * ballDiameter,
          getY() + (originY + ball.pos.y) * ballDiameter);
    }
  }
}