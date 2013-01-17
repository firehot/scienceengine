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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mazalearn.scienceengine.ScienceEngine;

public class ScreenUtils {

  private static Vector3 currentTouch = new Vector3();
  /**
   * Get a screenshot.
   * @param x - beginning x coordinate
   * @param y - beginning y coordinate
   * @param width - width
   * @param height - height
   * @param newWidth TODO
   * @param newHeight TODO
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
    // Make black color transparent
    int w = pixmap.getWidth();
    int h = pixmap.getHeight();
    Blending b = Pixmap.getBlending();
    Pixmap.setBlending(Blending.None);
    int c1rgba8888 = Color.rgba8888(Color.BLACK);
    int c2rgba8888 = Color.rgba8888(Color.YELLOW);
    for (int i = 0; i < w; i++) {
      for (int j = 0; j < h; j++) {
        int pixel = pixmap.getPixel(i, j);
        if (pixel == c1rgba8888 || pixel == c2rgba8888) {
          pixmap.drawPixel(i, j, 0);
        }
      }
    }
    Pixmap.setBlending(b);
  }

}
