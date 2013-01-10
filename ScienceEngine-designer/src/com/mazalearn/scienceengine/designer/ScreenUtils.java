package com.mazalearn.scienceengine.designer;

import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.utils.LevelUtil;

public class ScreenUtils {

  /**
   * Get a screenshot.
   * @param x - beginning x coordinate
   * @param y - beginning y coordinate
   * @param w - width
   * @param h - height
   * @param scale - how much to scale down - should be < 1.
   * @return screenshot as a pixmap
   */
  public static Pixmap getScreenshot(int x, int y, int w, int h, float scale) {
    Gdx.gl.glPixelStorei(GL10.GL_PACK_ALIGNMENT, 1);
  
    Pixmap screenShot = new Pixmap(w, h, Format.RGBA8888);
    ByteBuffer pixels = screenShot.getPixels();
    Gdx.gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, pixels);
    // Scaling should create a power of 2 texture for android
    Pixmap scaledPic = new Pixmap(LevelUtil.powerOf2Ceiling(w / scale), 
        LevelUtil.powerOf2Ceiling(h / scale), Format.RGBA8888);
    scaledPic.drawPixmap(screenShot, 0, 0, w, h, 
        0, 0, scaledPic.getWidth(), scaledPic.getHeight());
    screenShot.dispose();
    flipY(w, h, scaledPic);
    Gdx.app.log(ScienceEngine.LOG, "Screenshot: " + x + " x " + y);
    return scaledPic;
  }

  private static void flipY(int w, int h, Pixmap pixmap) {
    ByteBuffer pixels = pixmap.getPixels();
    final int numBytes = w * h * 4;
    byte[] lines = new byte[numBytes];
    final int numBytesPerLine = w * 4;
    for (int i = 0; i < h; i++) {
      pixels.position((h - i - 1) * numBytesPerLine);
      pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
    }
    pixels.clear();
    pixels.put(lines);
    pixels.clear();
  }
}
