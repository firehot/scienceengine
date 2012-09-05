package com.mazalearn.scienceengine.experiments.waves.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Align;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;
import com.mazalearn.scienceengine.experiments.waves.WaveModel.Ball;

public class Hand extends Image {
  private final Ball ball;
  float prevY, originY;
  private float ballDiameter;

  public Hand(TextureRegion region, Scaling scaling, Ball ball, float originX, 
      float originY, float ballDiameter) {
    super(region, scaling, Align.CENTER, "Hand");
    this.originY = originY;
    this.ball = ball;
    this.width *= 4; 
    this.height *= 4;
    this.x = (originX + ball.pos.x) * ballDiameter;
    this.ballDiameter = ballDiameter;
  }
  
  public boolean touchDown(float x, float y, int pointer) {
    prevY = y;
    return true;
  }

  public void touchDragged(float x, float y, int pointer) {
    ball.pos.y += (y - prevY) / ballDiameter;
    prevY = y;
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    this.y = (originY + ball.pos.y) * ballDiameter;
    super.draw(batch, parentAlpha);
  }
}