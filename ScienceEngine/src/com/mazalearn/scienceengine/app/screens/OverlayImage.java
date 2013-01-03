package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class OverlayImage extends Image {

  private Image overlayImage;

  public OverlayImage(Texture texture, Texture overlay) {
    super(texture);
    overlayImage = new Image(overlay);
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    overlayImage.setPosition(getX() + getWidth() / 2 - overlayImage.getWidth() / 2, 
        getY() + getHeight() / 2 - overlayImage.getHeight() / 2);
    overlayImage.draw(batch, parentAlpha * 0.40f);
  }

}
