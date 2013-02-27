package com.mazalearn.scienceengine.tutor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScreenComponent;

final class SuccessFailureImage extends Image {
  private String message;
  private final boolean success;
  private BitmapFont font;

  SuccessFailureImage(Texture texture, Skin skin, boolean success) {
    super(texture);
    this.setVisible(false);
    ScreenComponent.scaleSize(this, getWidth(), getHeight());
    this.success = success;
    font = skin.getFont("default-font");
  }

  public void show(String message, final IDoneCallback doneCallback) {
    // Middle of screen
    this.setPosition(ScreenComponent.VIEWPORT_WIDTH/2, ScreenComponent.VIEWPORT_HEIGHT/2);
    this.message = success ? message : "Continue";
    this.setVisible(true);
    float moveBy = ScreenComponent.getScaledY(success ? 10 : -5);
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
    if (doneCallback == null) return;
    
    this.addListener(new ClickListener() {
      public void clicked (InputEvent event, float x, float y) {
        for (EventListener l: getListeners()) {
          removeListener(l);
        }
        setVisible(false);
        doneCallback.done(true);
      }      
    });
  }

  public void draw(SpriteBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    Color c = font.getColor();
    font.setColor(Color.WHITE);
    font.draw(batch, message, getX() + getWidth()/2 - 30, getY() + getHeight()/2);
    font.setColor(c);
  }
}