package com.mazalearn.scienceengine.experiments.waves.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.mazalearn.scienceengine.experiments.waves.WaveModel.Ball;

public class Hand extends Image {
  private final Ball ball;
  float lastTouchedY, originX, originY;
  private int ballDiameter;

  public Hand(Texture texture, final Ball ball, float originX, float originY) {
    super(texture);
    this.setName("Hand");
    this.originX = originX;
    this.originY = originY;
    this.ball = ball;
    this.setWidth(this.getWidth() * 4); 
    this.setHeight(this.getHeight() * 4);
    this.addListener(new DragListener() {
      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        lastTouchedY = y;
        return true;
      }

      @Override
      public void touchDragged(InputEvent event, float x, float y, int pointer) {
        ball.pos.y += (y - lastTouchedY) / ballDiameter;
      }
      
    });
  }
  
  @Override
  public void act(float delta) {
    this.setY((originY + ball.pos.y) * ballDiameter);
    super.act(delta);
  }

  public void setBallDiameter(int ballDiameter) {
    this.ballDiameter = ballDiameter;
  }
}