package com.mazalearn.scienceengine.domains.waves.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.waves.WaveView;
import com.mazalearn.scienceengine.domains.waves.model.ComponentType;
import com.mazalearn.scienceengine.domains.waves.model.WaveBox;
import com.mazalearn.scienceengine.domains.waves.model.WaveBox.Ball;

public class WaveBoxActor extends Science2DActor {

  private final TextureRegion ballTextureRed, ballTextureBlue;
  private final float originX, originY;
  private int ballDiameter;
  private WaveView waveView;
  private WaveBox waveBox;
  
  public WaveBoxActor(Science2DBody waveBox, TextureRegion ballTextureRed, TextureRegion ballTextureBlue,
      float originX, float originY, WaveView waveView) {
    super(waveBox, null);
    this.waveBox = (WaveBox) waveBox;
    super.setName(ComponentType.WaveBox.name());
    this.waveView = waveView;
    this.ballTextureRed = ballTextureRed;
    this.ballTextureBlue = ballTextureBlue;
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
    this.ballDiameter = (int) (getWidth() / (waveBox.balls.length + 10));
    waveView.setBallDiameter(ballDiameter);
  }
  
  public int getBallDiameter() {
    return ballDiameter;
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    // Draw the balls
    int i = 1;
    for (Ball ball: waveBox.balls) {
      i = (i + 1) % 10;
      batch.draw(i == 0 ? ballTextureBlue : ballTextureRed, 
          getX() + (originX + ball.pos.x) * ballDiameter,
          getY() + (originY + ball.pos.y) * ballDiameter);
    }
  }
}