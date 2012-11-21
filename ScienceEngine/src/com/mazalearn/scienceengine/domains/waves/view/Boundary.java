package com.mazalearn.scienceengine.domains.waves.view;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.mazalearn.scienceengine.domains.waves.WaveModel.Ball;

public class Boundary extends Image {
  private final Ball ball;
  private final float originY;
  private int ballDiameter;
  private WaveBox waveBox;

  public Boundary(TextureRegion region, Ball ball, float originY) {
    super(region);
    this.setName("Boundary");
    this.originY = originY;
    this.ball = ball;
  }

  @Override
  public void act(float delta) {
    this.setY(waveBox.getY() + (originY + ball.pos.y) * ballDiameter);
    super.act(delta);
  }

  public void setBallDiameter(int ballDiameter) {
    this.ballDiameter = ballDiameter;
    this.setSize(ballDiameter, ballDiameter);
  }

  public void setWaveBox(WaveBox waveBox) {
    this.waveBox = waveBox;
  }
}