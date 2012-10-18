package com.mazalearn.scienceengine.app.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class LevelUtil {

  public static Texture getLevelThumbnail(String experimentName, int level) {
    FileHandle screenFile = 
        LevelUtil.getLevelFile(experimentName, ".png", level);      
    Pixmap pixmap;
    try {
      pixmap = new Pixmap(screenFile);
    } catch (GdxRuntimeException e) {
      pixmap = new Pixmap(LevelUtil.powerOf2Ceiling(Gdx.graphics.getWidth()/7.5f), 
          LevelUtil.powerOf2Ceiling(Gdx.graphics.getHeight()/7.5f), 
          Format.RGBA8888);
    }
    Texture texture = new Texture(pixmap);
    pixmap.dispose();
    return texture;
  }

  public static int powerOf2Ceiling(float value) {
    return 1 << (int) Math.ceil(Math.log(value) / Math.log(2));
  }

  public static FileHandle getLevelFile(String experimentName, String extension, int level) {
    return Gdx.files.internal("data/" + experimentName + "/" + level + extension);
  }

}
