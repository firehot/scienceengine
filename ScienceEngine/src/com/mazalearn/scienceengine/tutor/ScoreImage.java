package com.mazalearn.scienceengine.tutor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScreenComponent;

final class ScoreImage extends Image {
  private String message;
  private BitmapFont font;

  ScoreImage(TextureRegion textureRegion, Skin skin) {
    super(textureRegion);
    this.setVisible(false);
    ScreenComponent.scaleSize(this, getWidth() * 2, getHeight() * 2);
    font = skin.getFont("default-font");
  }

  public void show(String message) {
    // Middle of screen
    this.setX(ScreenComponent.VIEWPORT_WIDTH/2);
    this.setY(ScreenComponent.VIEWPORT_HEIGHT/2);
    this.message = message;
    this.setVisible(true);
    this.addAction(
          Actions.sequence(
              Actions.alpha(0),
              Actions.alpha(1, 0.25f),
              Actions.delay(2),
              new Action() {
                @Override
                public boolean act(float delta) {
                  setVisible(false);
                  return true;
                }
              }
          )
        );
  }

  public void draw(SpriteBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    Color c = font.getColor();
    font.setColor(Color.WHITE);
    font.draw(batch, message, getX() + getWidth()/2 - 30, getY() + getHeight()/2);
    font.setColor(c);
  }
}