package com.mazalearn.scienceengine.experiments.waves.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;
import com.mazalearn.scienceengine.experiments.waves.WaveModel.Ball;

public class Hand extends Image {
  private final Ball ball;
  float prevY, originY;

  public Hand(TextureRegion region, Scaling scaling, Ball ball, float originX, float originY) {
    super(region, scaling);
    this.originY = originY;
    this.ball = ball;
    width *= 4; height *= 4;
    this.x = originX + ball.pos.x;
  }
  
  public boolean touchDown(float x, float y, int pointer) {
    prevY = y;
    return true;
  }

  public void touchDragged(float x, float y, int pointer) {
    ball.pos.y += y - prevY;
    prevY = y;
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    this.y = originY + ball.pos.y;
    super.draw(batch, parentAlpha);
  }
}