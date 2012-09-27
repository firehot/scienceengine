package com.mazalearn.scienceengine.core.probe;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class ProbeImage extends Image {
  private static Texture QUESTION_MARK = new Texture("images/questionmark.png");
  private float increment = 0.01f;
  private float alpha = 1;
  public ProbeImage() {
    super(QUESTION_MARK);
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    Color c = batch.getColor();
    batch.setColor(c.r, c.g, c.b, alpha);
    super.draw(batch, alpha);
    batch.setColor(c);
    alpha += increment;
    if (alpha > 1 - increment || alpha <= 0.5f) {
      increment = -increment;
    }
  }

}
