package com.mazalearn.scienceengine.core.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mazalearn.scienceengine.ScreenComponent;

public class Snapshotter extends Actor {
  
  private TextureRegion highlight;

  public Snapshotter() {
    highlight = createTexture();
    setPosition(0, 0);
    setSize(ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT);
  }
  
  private static TextureRegion createTexture() {
    Pixmap pixmap = new Pixmap(10, 10 , Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.CLEAR);
    pixmap.fillRectangle(0, 0, 10, 10);
    TextureRegion textureRegion = new TextureRegion(new Texture(pixmap));
    pixmap.dispose();
    return textureRegion;
  }
  
  public void draw (SpriteBatch batch, float parentAlpha) {
    batch.draw(highlight, getX(), getY(), getWidth(), getHeight());
  }
  
}
