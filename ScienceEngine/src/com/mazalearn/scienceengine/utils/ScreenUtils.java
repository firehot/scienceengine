package com.mazalearn.scienceengine.utils;

import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;

public class ScreenUtils {

  /**
   * Get a screenshot.
   * @param x - beginning x coordinate
   * @param y - beginning y coordinate
   * @param w - width
   * @param h - height
   * @param flipY - whether y to be flipped - required for saving to disk.
   * @return screenshot as a pixmap
   */
  public static Pixmap getScreenshot(int x, int y, int w, int h, boolean flipY) {
    Gdx.gl.glPixelStorei(GL10.GL_PACK_ALIGNMENT, 1);
  
    Pixmap screenShot = new Pixmap(w, h, Format.RGBA8888);
    ByteBuffer pixels = screenShot.getPixels();
    Gdx.gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, pixels);
  
    final int numBytes = w * h * 4;
    byte[] lines = new byte[numBytes];
    if (flipY) {
      final int numBytesPerLine = w * 4;
      for (int i = 0; i < h; i++) {
        pixels.position((h - i - 1) * numBytesPerLine);
        pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
      }
      pixels.clear();
      pixels.put(lines);
    } else {
      pixels.clear();
      pixels.get(lines);
    }
  
    return screenShot;
  }

  /**
   * Scale down pixmap to create a thumbnail.
   * @param pixmap - pixmap to be scaled down
   * @param scale - amount to be scaled
   * @return scaled down thumbnail as a pixmap
   */
  public static Pixmap createThumbnail(Pixmap pixmap, int scale) {
    Pixmap thumbnail = new Pixmap(pixmap.getWidth() / scale, 
        pixmap.getHeight() / scale, Format.RGBA8888);
    thumbnail.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), 
        0, 0, thumbnail.getWidth(), thumbnail.getHeight());
    return thumbnail;
  }

}
