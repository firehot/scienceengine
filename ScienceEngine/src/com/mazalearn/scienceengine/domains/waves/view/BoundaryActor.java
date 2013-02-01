package com.mazalearn.scienceengine.domains.waves.view;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.waves.model.WaveBox.Ball;

public class BoundaryActor extends Science2DActor {
  private final Ball ball;
  private final float originY;
  private int ballDiameter;
  private WaveBoxActor waveBoxActor;
  
  public BoundaryActor(Science2DBody boundary, TextureRegion region, Ball ball, float originY) {
    super(boundary, region);
    this.originY = originY;
    this.ball = ball;
  }
  
  @Override
  public void act(float delta) {
    this.setY(waveBoxActor.getY() + (originY + ball.pos.y) * ballDiameter);
    //super.act(delta);
  }

  public void setBallDiameter(int ballDiameter) {
    this.ballDiameter = ballDiameter;
    this.setSize(ballDiameter, ballDiameter);
  }

  public void setWaveBox(WaveBoxActor waveBoxActor) {
    this.waveBoxActor = waveBoxActor;
  }
}