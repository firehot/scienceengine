package com.mazalearn.scienceengine.guru;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScreenComponent;

final class ScoreImage extends Image {
  private String message;
  private final boolean success;
  private BitmapFont font;

  ScoreImage(Texture texture, Skin skin, boolean success) {
    super(texture);
    this.setVisible(false);
    setSize(ScreenComponent.getScaledX(getWidth()), ScreenComponent.getScaledY(getHeight()));
    this.success = success;
    font = skin.getFont("default-font");
  }

  public void show(String message) {
    // Middle of screen
    this.setX(ScreenComponent.VIEWPORT_WIDTH/2);
    this.setY(ScreenComponent.VIEWPORT_HEIGHT/2);
    this.message = message;
    this.setVisible(true);
    float moveBy = ScreenComponent.getScaledY(success ? 10 : -10);
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
    font.draw(batch, message, getX() + getWidth()/2 - 10, getY() + getHeight()/2);
    font.setColor(c);
  }
}