package com.mazalearn.scienceengine.core.guru;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

final class ScoreImage extends Image {
  private int score;
  private final boolean success;
  private BitmapFont font;

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
    float moveBy = success ? 10 : -10;
    this.setRotation(-5f);
    this.addAction(
        Actions.repeat(20,
            Actions.sequence(
                Actions.parallel(
                    Actions.rotateBy(10f, 0.1f), 
                    Actions.moveBy(0f, moveBy, 0.1f)),
                Actions.parallel(
                    Actions.rotateBy(-10f, 0.1f), 
                    Actions.moveBy(0f, moveBy, 0.1f))
                )
            )
        );
  }

  public void draw(SpriteBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    Color c = font.getColor();
    font.setColor(Color.BLACK);
    font.draw(batch, String.valueOf(score), getX() + getWidth()/2 - 10, getY() + getHeight()/2);
    font.setColor(c);
  }
}