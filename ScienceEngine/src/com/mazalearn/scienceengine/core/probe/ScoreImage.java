package com.mazalearn.scienceengine.core.probe;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

final class ScoreImage extends Image {
  private int score;
  private final boolean success;
  private BitmapFont font;
  private float increment = 0.015f;

  ScoreImage(Texture texture, Skin skin, boolean success) {
    super(texture);
    this.setVisible(false);
    this.success = success;
    font = skin.getFont("default-font");
  }

  public void show(float x, float y, int score) {
    this.setX(x);
    this.setY(y);
    this.score = score;
    this.setVisible(true);
  }

  public void act(float delta) {
    this.setY(this.getY() + (success ? 2 : -2));
    this.setRotation(this.getRotation() + increment);
    if (this.getRotation() >= 5) {
      increment = -1;
    } else if (this.getRotation() <= -5) {
      increment = 1;
    }
  }

  public void draw(SpriteBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    Color c = font.getColor();
    font.setColor(Color.BLACK);
    font.draw(batch, String.valueOf(score), getX() + getWidth()/2 - 10, getY() + getHeight()/2);
    font.setColor(c);
  }
}