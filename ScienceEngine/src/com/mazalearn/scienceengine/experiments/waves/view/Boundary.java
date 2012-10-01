package com.mazalearn.scienceengine.experiments.waves.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.mazalearn.scienceengine.experiments.waves.WaveModel.Ball;

public class Boundary extends Image {
  private final Ball ball;
  private final float originY;
  private float ballDiameter;

  public Boundary(TextureRegion region, Ball ball, float originX, 
      float originY, float ballDiameter) {
    super(region);
    this.setName("Boundary");
    this.originY = originY;
    this.ball = ball;
    this.setX((originX + ball.pos.x) * ballDiameter);
    this.ballDiameter = ballDiameter;
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    this.setY((originY + ball.pos.y) * ballDiameter);
    super.draw(batch, parentAlpha);
  }
}