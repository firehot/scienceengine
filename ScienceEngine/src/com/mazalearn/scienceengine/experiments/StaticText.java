package com.mazalearn.scienceengine.experiments;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

public class StaticText extends Widget {
  
  Texture backgroundTexture;
  
  StaticText(String text) {
    super();
    // Use light-gray background color
    Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
    pixmap.setColor(Color.BLUE);
    pixmap.fillRectangle(0,  0, 1, 1);
    backgroundTexture = new Texture(pixmap);
    pixmap.dispose();
  }
  
  StaticText() {
    this("");
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    // Draw background
    batch.draw(backgroundTexture, this.x, this.y, this.width, this.height);
  }
  
}
