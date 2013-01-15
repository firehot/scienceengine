package com.mazalearn.scienceengine;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.designer.LevelEditor;
import com.mazalearn.scienceengine.designer.PngWriter;

public class NonWebPlatformAdapter extends AbstractPlatformAdapter {
  
  public NonWebPlatformAdapter(Platform platform) {
    super(platform);
  }

  @Override
  public Stage createLevelEditor(IScience2DController science2DController,
      AbstractScreen screen) {
    return new LevelEditor(science2DController, screen);
  }

  @Override
  public void getBytes(Pixmap pixmap, byte[] lines) {
    ByteBuffer pixels = pixmap.getPixels();
    pixels.get(lines);
  }

  @Override
  public void setBytes(Pixmap pixmap, byte[] lines) {
    ByteBuffer pixels = pixmap.getPixels();
    pixels.clear();
    pixels.put(lines);
    pixels.clear();   
  }
  
  @Override
  public byte[] getPngBytes(Pixmap snapshot) {
    ScreenUtils.makeBlackTransparent(snapshot);
    try {
      return PngWriter.generateImage(snapshot);
    } catch (IOException e) {
      throw new GdxRuntimeException(e);
    }
  }
}