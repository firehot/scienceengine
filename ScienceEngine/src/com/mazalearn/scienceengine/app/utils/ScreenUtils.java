package com.mazalearn.scienceengine.app.utils;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Filter;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;

public class ScreenUtils {

  private static Vector3 currentTouch = new Vector3();
  /**
   * Get a screenshot.
   * @param x - beginning x stage coordinate
   * @param y - beginning y stage coordinate
   * @param width - stage width
   * @param height - stage height
   * @param newWidth - width of new image in pixels
   * @param newHeight - height of new image in pixels
   * @param stage 
   * @return screenshot as a pixmap
   */
  public static Pixmap getScreenshot(float x, float y, float width, 
      float height, int newWidth, int newHeight, Stage stage, 
      boolean makeBlackTransparent) {
    // World coords of actor bottom left
    currentTouch.set(x, y, 0);
    // Screen coords of actor bottom left
    stage.getCamera().project(currentTouch);
    int sx = Math.round(currentTouch.x);
    int sy = Math.round(currentTouch.y);
    // World coords of actor top right
    currentTouch.set(x + width, y + height, 0);
    // Screen coords of actor top right
    stage.getCamera().project(currentTouch);
    int sw = Math.round(currentTouch.x) - sx;
    int sh = Math.round(currentTouch.y) - sy;
    Gdx.gl.glPixelStorei(GL10.GL_PACK_ALIGNMENT, 1);
    Blending b = Pixmap.getBlending();
    Pixmap.setBlending(Blending.None);
  
    Pixmap screenShot = new Pixmap(sw, sh, Format.RGBA8888);
    Buffer pixels = screenShot.getPixels();
    // Following workaround for GWT
    if (!(pixels instanceof ByteBuffer)) {
      byte[] bytes = new byte[sw * sh * 4];
      Gdx.gl.glReadPixels(sx, sy, sw, sh, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, 
          ByteBuffer.wrap(bytes));
      ScienceEngine.getPlatformAdapter().putBytes(screenShot, bytes);
    } else {
      Gdx.gl.glReadPixels(sx, sy, sw, sh, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, pixels);
    }
    Pixmap.setFilter(Filter.NearestNeighbour);
    Pixmap scaledPic = new Pixmap(newWidth, newHeight, Format.RGBA8888);
    scaledPic.drawPixmap(screenShot, 0, 0, sw, sh, 0, 0, newWidth, newHeight);
    screenShot.dispose();
    if (makeBlackTransparent) {
      removeBlackAndYellow(scaledPic);
    }
    flipY(scaledPic);
    Gdx.app.log(ScienceEngine.LOG, "Screenshot: " + newWidth + " x " + newHeight);
    Pixmap.setBlending(b);
    return scaledPic;
  }

  public static void flipY(Pixmap pixmap) {
    int w = pixmap.getWidth();
    int h = pixmap.getHeight();
    final int numBytes = w * h * 4;
    byte[] lines = new byte[numBytes];
    ScienceEngine.getPlatformAdapter().getBytes(pixmap, lines);
    final int numBytesPerLine = w * 4;
    for (int i = 0; i < (h + 1)/ 2; i++) {
      swapLines(lines, i, h - i -1, numBytesPerLine);
    }
    ScienceEngine.getPlatformAdapter().putBytes(pixmap, lines);
  }
  
  private static void swapLines(byte[] lines, int line1, int line2, int numBytesPerLine) {
    int l1 = line1 * numBytesPerLine;
    int l2 = line2 * numBytesPerLine;
    for (int i = 0; i < numBytesPerLine; i++) {
      byte b = lines[l1 + i];
      lines[l1 + i] = lines[l2 + i];
      lines[l2 + i] = b;
    }
  }

  private static void removeBlackAndYellow(Pixmap pixmap) {
    // Make black and yellow colors transparent
    int w = pixmap.getWidth();
    int h = pixmap.getHeight();
    Pixmap.setBlending(Blending.None);
    int blackColor = Color.rgba8888(Color.BLACK);
    int yellowColor = Color.rgba8888(Color.YELLOW);
    for (int i = 0; i < w; i++) {
      for (int j = 0; j < h; j++) {
        // Force alpha to 1 for comparison - not sure why alpha is changing for snapshot
        int pixel = pixmap.getPixel(i, j) | 0xFF;
        if (pixel == blackColor || pixel == yellowColor) {
          pixmap.drawPixel(i, j, 0);
        }
      }
    }
  }

  public static TextureRegion createTextureRegion(float width, float height, Color color) {
    Pixmap pixmap = new Pixmap(1, 1 , Pixmap.Format.RGBA8888);
    pixmap.setColor(color);
    pixmap.fillRectangle(0, 0, 1, 1);
    TextureRegion textureRegion = new TextureRegion(new Texture(pixmap), (int) width, (int) height);
    pixmap.dispose();
    return textureRegion;
  }

  public static Label createLabel(String text, 
      float x, float y, float width, float height, LabelStyle labelStyle) {
    Label nameLabel = new Label(text, labelStyle);
    nameLabel.setWrap(true);
    nameLabel.setAlignment(Align.center, Align.center);
    ScreenComponent.scalePositionAndSize(nameLabel, x, y, width, height);
    return nameLabel;
  }

  public static TextButton createImageButton(TextureRegion textureRegion, Skin skin) {
    TextureRegionDrawable image = 
        new TextureRegionDrawable(textureRegion);
    TextButton activityThumb = new TextButton("", skin) {
      @Override
      public void drawBackground(SpriteBatch batch, float parentAlpha) {
        getBackground().draw(batch, getX()+5, getY()+5, getWidth()-10, getHeight()-10);
      }
    };
    activityThumb.setBackground(image);
    return activityThumb;
  }

  public static TextButton createTextButton(String text, 
      float x, float y, float width, float height, TextButtonStyle textButtonStyle) {
    TextButton button = new TextButton(text, textButtonStyle);
    button.getLabel().setWrap(true);
    button.getLabel().setAlignment(Align.center, Align.center);
    ScreenComponent.scaleSize(button, width, height);
    button.setPosition(x, y);
    return button;
  }

  public static TextButton createCheckBox(String text, 
      float x, float y, float width, float height, CheckBoxStyle checkBoxStyle) {
    CheckBox button = new CheckBox(text, checkBoxStyle);
    button.getLabel().setWrap(true);
    button.getLabel().setAlignment(Align.center, Align.center);
    ScreenComponent.scaleSize(button, width, height);
    button.setPosition(x, y);
    button.getCell(button.getImage()).width(button.getImage().getWidth() * 2).height(button.getImage().getHeight() * 2);
    button.getCell(button.getLabel()).width(button.getWidth() - button.getImage().getWidth() * 2);
    return button;
  }

  public static void createProgressPercentageBar(LabelStyle labelStyle,
      TextButton thumbnail, float percent, int width) {
    TextureRegion bar = createTextureRegion(10, 10, Color.GRAY);
    Image fullBar = new Image(bar);
    ScreenComponent.scalePositionAndSize(fullBar, 10, 20, width - 20, 10);
    thumbnail.addActor(fullBar);
    Image successBar = new Image(createTextureRegion(10, 10, Color.RED));
    ScreenComponent.scalePositionAndSize(successBar, 10, 20, percent * (width - 20) / 100f, 10);
    thumbnail.addActor(successBar);
    Label percentLabel = new Label(String.valueOf(Math.round(percent)) + "%", labelStyle);
    percentLabel.setAlignment(Align.center, Align.center);
    ScreenComponent.scalePositionAndSize(percentLabel, 5, 12, 40, 20);
    thumbnail.addActor(percentLabel);
  }

  public static TextButton createImageMessageBox(Skin skin, String textureName) {
    TextureRegion textureRegion = ScienceEngine.getTextureRegion(textureName);
    TextButton message = createImageButton(textureRegion, skin);
    message.getLabel().setWrap(true);
    message.getCell(message.getLabel()).pad(80, 40, 70, 40);
    TextButtonStyle tbs = new TextButtonStyle(skin.get("clear", TextButtonStyle.class));
    tbs.fontColor = Color.BLACK;
    message.setStyle(tbs);
    message.setWidth(250);
    message.setHeight(250);
    message.setPosition(ScreenComponent.ImageMessageBox.getX(message.getWidth()),
        ScreenComponent.ImageMessageBox.getY(message.getHeight()));
    return message;
  }

}
