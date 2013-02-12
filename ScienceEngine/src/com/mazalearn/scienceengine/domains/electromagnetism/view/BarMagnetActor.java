package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.model.Science2DBody.MovementMode;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.BarMagnet;

public class BarMagnetActor extends Science2DActor {
  private final BarMagnet barMagnet;
  private BitmapFont font;
  private Vector2 newPos = new Vector2();
  
  public BarMagnetActor(Science2DBody body, TextureRegion textureRegion, 
      BitmapFont font) {
    super(body, textureRegion);
    this.barMagnet = (BarMagnet) body;
    this.font = font;
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    Color c = batch.getColor();
    float alpha = 0.5f + barMagnet.getStrength() / (2 * BarMagnet.MAX_STRENGTH);
    batch.setColor(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, alpha);
    super.draw(batch, parentAlpha);
    batch.setColor(c);
    if (MovementMode.valueOf(barMagnet.getMovementMode()) == MovementMode.Rotate) { // Display RPM
      drawRpm(batch, parentAlpha);
    }
  }

  private void drawRpm(SpriteBatch batch, float parentAlpha) {
    font.setColor(1f, 1f, 1f, parentAlpha);
    int angularVelocity = Math.round(barMagnet.getAngularVelocity());
    String rpm = String.valueOf(angularVelocity);
    newPos.set(barMagnet.getWorldCenter()).mul(ScreenComponent.PIXELS_PER_M);
    // Create space for text - in screen coords and always left to right
    newPos.add(-10, 5);
    font.draw(batch, rpm, newPos.x, newPos.y);
  }
}