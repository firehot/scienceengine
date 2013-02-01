package com.mazalearn.scienceengine.domains.waves.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.waves.model.WaveBox.Ball;

public class WaveMakerActor extends Science2DActor {
  private final Ball ball;
  float lastTouchedY, originX, originY;
  private int ballDiameter;

  public WaveMakerActor(Science2DBody hand, final Ball ball, float originX, float originY) {
    super(hand, new TextureRegion(new Texture("image-atlases/hand-pointer1.png")));
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
  }

  public void setBallDiameter(int ballDiameter) {
    this.ballDiameter = ballDiameter;
  }
}