package com.mazalearn.scienceengine.tutor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;

public class ImageMessageBox extends TextButton {
  
  private TextButton nextButton, prevButton;
  protected static final int SCALE = 60;
  private Image closeImage;
  private TextureRegionDrawable background;

  public ImageMessageBox(Skin skin, String textureName, final Actor parentActor) {
    super("", skin);
    background = new TextureRegionDrawable(ScienceEngine.getTextureRegion(textureName));
    setBackground(background);
    getLabel().setWrap(true);
    TextButtonStyle tbs = new TextButtonStyle(skin.get("clear", TextButtonStyle.class));
    tbs.fontColor = Color.BLACK;
    setStyle(tbs);
    setWidth(250);
    setHeight(250);
    setPosition(ScreenComponent.ImageMessageBox.getX(getWidth()),
        ScreenComponent.ImageMessageBox.getY(getHeight()));
    
    closeImage = new Image(ScienceEngine.getTextureRegion("close"));
    ScreenComponent.scaleSize(closeImage, closeImage.getWidth() * 0.5f, closeImage.getHeight() * 0.5f);
    closeImage.addListener(new ClickListener() {
      @Override 
      public void clicked (InputEvent event, float x, float y) {
        if (parentActor == null) {
          setVisible(false);
        } else {
          getStage().getRoot().removeActor(parentActor);
        }
      }      
    });
    addActor(closeImage);
    
    nextButton = new TextButton(ScienceEngine.getMsg().getString("ScienceEngine.Next"), skin);
    nextButton.setPosition(5, 5);
    addActor(nextButton);
    
    prevButton = new TextButton(ScienceEngine.getMsg().getString("ScienceEngine.Prev"), skin);
    prevButton.setPosition(5, 5);
    addActor(prevButton);    
  }

  @Override
  public void drawBackground(SpriteBatch batch, float parentAlpha) {
    getBackground().draw(batch, getX()+5, getY()+5, getWidth()-10, getHeight()-10);
  }

  public TextButton getNextButton() {
    return nextButton;
  }

  public TextButton getPrevButton() {
    return prevButton;
  }

  public void setTextAndResize(String text) {
    setBackground(background);
    // Set size in a 3:1 aspect ratio
    float semiPerimeter = (float) Math.sqrt(text.length());
    float h = semiPerimeter * SCALE / 4 + 30; // To hold Buttons
    float w = semiPerimeter * SCALE * 3 / 4 + 40;
    if (w > ScreenComponent.VIEWPORT_WIDTH * 0.9f) {
      w = ScreenComponent.VIEWPORT_WIDTH * 0.9f;
      h *= 1.1f;
    }
    ScreenComponent.scaleSize(this, w, h);
    setSize(w, h);
    super.setText(text);
    getCell(getLabel()).pad(0.15f * h, 0.12f * w, 0.1f *h, 0.1f * w);
    getLabel().setAlignment(Align.center, Align.left);
    positionButtons();
  }

  private void positionButtons() {
    closeImage.setPosition(getWidth() - 2 * closeImage.getWidth(), getHeight() - closeImage.getHeight());
    nextButton.setPosition(getWidth() / 2 + nextButton.getWidth() / 2, 5);
    prevButton.setPosition(getWidth() / 2 - 2 * prevButton.getWidth() / 2, 5);
  }

  public void setImageAndResize(TextureRegionDrawable drawable) {
    super.setText("");
    setBackground(drawable);
    setSize(drawable.getMinWidth(), getMinHeight());
    positionButtons();
  }
}